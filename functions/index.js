// onCall ve setGlobalOptions fonksiyonlarını doğru paketlerden içe aktarıyoruz.
const {onCall} = require("firebase-functions/v2/https");
const {setGlobalOptions} = require("firebase-functions/v2");
const {onDocumentUpdated} = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

// Firebase Admin SDK'sını başlatıyoruz.
// Bu, Cloud Function'ımızın Firestore ve Auth gibi servislere erişmesini sağlar.
admin.initializeApp();

// Firestore veritabanına bir referans alıyoruz.
const db = admin.firestore();

// (İsteğe bağlı) Tüm fonksiyonlar için genel ayarlar.
setGlobalOptions({ maxInstances: 10 });

/**
 * 'Users' koleksiyonundaki bir döküman güncellendiğinde tetiklenir.
 * Özellikle 'following' dizisindeki değişiklikleri dinler.
 */
exports.updateFriendsOnFollow = onDocumentUpdated("Users/{userId}", async (event) => {
  // Değişiklikten önceki ve sonraki verileri alıyoruz.
  const beforeData = event.data.before.data();
  const afterData = event.data.after.data();
  const userId = event.params.userId; // Değişikliğin yapıldığı kullanıcının ID'si

  // Eğer 'following' listesi değişmemişse fonksiyondan çık.
  if (JSON.stringify(beforeData.following) === JSON.stringify(afterData.following)) {
    console.log(`Kullanıcı ${userId} için 'following' listesi değişmedi, işlem yapılmadı.`);
    return null;
  }

  // Listeleri Set'e çevirerek farkı bulmayı kolaylaştırıyoruz.
  const beforeFollowingSet = new Set(beforeData.following || []);
  const afterFollowingSet = new Set(afterData.following || []);

  // Yeni takip edilenleri bul (sonraki listede olup öncekinde olmayan)
  const followedId = [...afterFollowingSet].find((id) => !beforeFollowingSet.has(id));

  // Takipten çıkılanları bul (önceki listede olup sonrakinde olmayan)
  const unfollowedId = [...beforeFollowingSet].find((id) => !afterFollowingSet.has(id));

  // --- YENİ BİR TAKİP VARSA ---
  if (followedId) {
    console.log(`Kullanıcı ${userId}, ${followedId}'ı takibe aldı. Karşılıklı takip kontrol ediliyor...`);
    
    // Takip edilen kişinin dökümanını oku
    const followedUserDoc = await db.collection("Users").doc(followedId).get();
    if (!followedUserDoc.exists) {
      console.log(`${followedId} kullanıcısı bulunamadı.`);
      return null;
    }
    
    const followedUserData = followedUserDoc.data();
    // Karşı taraf da beni takip ediyor mu? (Karşılıklı takip kontrolü)
    if (followedUserData.following && followedUserData.following.includes(userId)) {
      console.log(`Karşılıklı takip tespit edildi! ${userId} ve ${followedId} arkadaş olarak ekleniyor.`);
      
      // Her iki kullanıcının da 'friends' listesini GÜVENLİ bir şekilde güncelle.
      const batch = db.batch();
      
      // Kendi friends listeme yeni arkadaşımı ekle
      const myRef = db.collection("Users").doc(userId);
      batch.update(myRef, {friends: admin.firestore.FieldValue.arrayUnion(followedId)});
      
      // Arkadaşımın friends listesine beni ekle
      const friendRef = db.collection("Users").doc(followedId);
      batch.update(friendRef, {friends: admin.firestore.FieldValue.arrayUnion(userId)});
      
      await batch.commit();
      console.log("Arkadaş listeleri başarıyla güncellendi.");
      
    } else {
      console.log(`${followedId} henüz ${userId}'ı takip etmiyor. Arkadaş eklenmedi.`);
    }
  }

  // --- BİR TAKİPTEN ÇIKMA VARSA ---
  if (unfollowedId) {
    console.log(`Kullanıcı ${userId}, ${unfollowedId}'ı takipten çıktı. Arkadaşlıktan çıkarılıyor...`);
    
    // Her iki kullanıcının da 'friends' listesinden birbirlerini çıkar.
    const batch = db.batch();
    
    // Kendi friends listemden eski arkadaşımı çıkar
    const myRef = db.collection("Users").doc(userId);
    batch.update(myRef, {friends: admin.firestore.FieldValue.arrayRemove(unfollowedId)});
    
    // Eski arkadaşımın friends listesinden beni çıkar
    const friendRef = db.collection("Users").doc(unfollowedId);
    batch.update(friendRef, {friends: admin.firestore.FieldValue.arrayRemove(userId)});
    
    await batch.commit();
    console.log("Arkadaş listeleri başarıyla güncellendi (çıkarma).");
  }

  return null;
});