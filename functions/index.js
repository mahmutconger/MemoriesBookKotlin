// onCall ve setGlobalOptions fonksiyonlarını doğru paketlerden içe aktarıyoruz.
const {onCall} = require("firebase-functions/v2/https");
const {onRequest} = require("firebase-functions/v2/https"); 
const {setGlobalOptions} = require("firebase-functions/v2");
const {onDocumentUpdated, onDocumentCreated} = require("firebase-functions/v2/firestore"); 
const {onDocumentDeleted} = require("firebase-functions/v2/firestore");
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

//BİLDİRİM MEKANİZMASI


exports.sendChatNotification = onDocumentCreated("chats/{chatRoomId}/messages/{messageId}", async (event) => {
  const messageData = event.data.data();
  const chatRoomId = event.params.chatRoomId;
  
  const senderId = messageData.senderId;
  const receiverId = chatRoomId.replace(senderId, "").replace("_", "");
  
  if (!receiverId) {
    console.log("Alıcı ID'si bulunamadı.");
    return null;
  }

  // 1. Alıcının dökümanını okuyup FCM token'ını al.
  const receiverDoc = await db.collection("Users").doc(receiverId).get();
  if (!receiverDoc.exists || !receiverDoc.data().fcmToken) {
    console.log("Alıcının token'ı bulunamadı.");
    return null;
  }
  const fcmToken = receiverDoc.data().fcmToken;

  // 2. Gönderenin profilinden kullanıcı adını al.
  const senderDoc = await db.collection("Users").doc(senderId).get();
  const senderName = senderDoc.data().username || "Birisi";
  
  // --- DEĞİŞİKLİK BURADA: BİLDİRİM PAKETİNİN DOĞRU YAPISI ---
  const message = {
    // Bu 'notification' ve 'data' alanları, paketin en üst seviyesinde olabilir.
    notification: {
      title: senderName,
      body: messageData.text,
    },
    data: {
      "click_action": "FLUTTER_NOTIFICATION_CLICK",
      "chat_partner_id": senderId,
    },
    // 'android' gibi platforma özel ayarlar, en üst seviyede bu şekilde belirtilir.
    android: {
      notification: {
        sound: "default",
        channel_id: "chat_messages_channel", // Android 8+ için kanal ID'miz
      },
    },
    token: fcmToken, // Hedef cihazın token'ı
  };

  // 4. Bildirimi 'sendToDevice' yerine 'send' ile gönder!
  try {
    await admin.messaging().send(message); // sendToDevice(token, payload) yerine send(message)
    console.log("Bildirim başarıyla gönderildi.");
  } catch (error) {
    console.error("Bildirim gönderme hatası:", error);
  }
  return null;
});


exports.onUserUpdate = onDocumentUpdated("Users/{userId}", async (event) => {
  const beforeData = event.data.before.data();
  const afterData = event.data.after.data();

  // Eğer username veya photoUrl değişmediyse, bir şey yapma.
  if (beforeData.username === afterData.username && beforeData.photoUrl === afterData.photoUrl) {
    console.log("Kullanıcı bilgisi değişmedi, güncelleme yapılmadı.");
    return null;
  }

  const userId = event.params.userId;
  const newUsername = afterData.username;
  const newPhotoUrl = afterData.photoUrl;
  
  const batch = db.batch();

  // 1. Adım: Bu kullanıcının tüm postlarını bul ve güncelleme için batch'e ekle.
  const postsQuery = db.collection("posts").where("authorId", "==", userId);
  const postsSnapshot = await postsQuery.get();
  postsSnapshot.forEach((doc) => {
    batch.update(doc.ref, {
      authorUsername: newUsername,
      authorPhotoUrl: newPhotoUrl,
    });
  });

  // 2. Adım: Bu kullanıcının tüm yorumlarını bul (Collection Group Query) ve güncelle.
  // Bu, veritabanındaki TÜM 'comments' alt koleksiyonlarını tarar.
  const commentsQuery = db.collectionGroup("comments").where("userId", "==", userId);
  const commentsSnapshot = await commentsQuery.get();
  commentsSnapshot.forEach((doc) => {
    batch.update(doc.ref, {
      username: newUsername,
      userPhotoUrl: newPhotoUrl,
    });
  });

  // 3. Adım: Tüm güncellemeleri tek seferde yap.
  await batch.commit();
  console.log(`Kullanıcı ${userId} için ${postsSnapshot.size} post ve ${commentsSnapshot.size} yorum güncellendi.`);
  return null;
});

exports.onPostDelete = onDocumentDeleted("posts/{postId}", (event) => {
  // 1. Silinen postun verisini alıyoruz.
  const postData = event.data.data();
  if (!postData) {
    console.log("Silinen postun verisi bulunamadı.");
    return null;
  }
  
  console.log(`Post ${event.params.postId} silindi. Medya dosyaları temizleniyor...`);

  const storage = admin.storage();
  const bucket = storage.bucket(); // Varsayılan storage bucket'ını al

  // 2. Silinecek tüm URL'leri tek bir listede toplayalım.
  const urlsToDelete = [];
  if (postData.mediaUrls && Array.isArray(postData.mediaUrls)) {
    urlsToDelete.push(...postData.mediaUrls);
  }
  if (postData.thumbnailUrl) {
    urlsToDelete.push(postData.thumbnailUrl);
  }

  if (urlsToDelete.length === 0) {
    console.log("Silinecek medya dosyası bulunamadı.");
    return null;
  }
  
  // 3. Her bir URL için silme işlemini başlat.
  const deletePromises = urlsToDelete.map((url) => {
    try {
      // Firebase Storage URL'sinden dosya yolunu (path) çıkarmamız gerekiyor.
      // Örnek URL: https://firebasestorage.googleapis.com/v0/b/your-project-id.appspot.com/o/media%2Ffile.jpg?alt=media&token=...
      // Bize sadece "media/file.jpg" kısmı lazım.
      const decodedUrl = decodeURIComponent(url);
      const filePath = decodedUrl.split("/o/")[1].split("?")[0];
      
      console.log(`Storage'dan siliniyor: ${filePath}`);
      return bucket.file(filePath).delete();
    } catch (error) {
      console.error(`URL'den dosya yolu çıkarılamadı veya geçersiz URL: ${url}`, error);
      return null;
    }
  });

  // Tüm silme işlemlerinin bitmesini bekle.
  return Promise.all(deletePromises)
      .then(() => console.log("Tüm medya dosyaları başarıyla silindi."))
      .catch((err) => console.error("Medya dosyaları silinirken bir hata oluştu:", err));
});