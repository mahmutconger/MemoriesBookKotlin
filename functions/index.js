// onCall ve setGlobalOptions fonksiyonlarını doğru paketlerden içe aktarıyoruz.
const {onCall} = require("firebase-functions/v2/https");
const {onRequest} = require("firebase-functions/v2/https"); 
const {setGlobalOptions} = require("firebase-functions/v2");
const {onDocumentUpdated, onDocumentCreated} = require("firebase-functions/v2/firestore"); 
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

exports.migratePostsData =  onRequest(async (request, response) => {
  console.log("Post veri taşıma script'i başlatıldı.");


  const postsRef = db.collection("posts");
  const snapshot = await postsRef.get();

  if (snapshot.empty) {
    console.log("Güncellenecek post bulunamadı.");
    return {message: "Güncellenecek post bulunamadı."};
  }

  // Firestore'da batch (toplu yazma) en fazla 500 işlem alabilir.
  // Binlerce dökümanın varsa bu kodu parçalara bölmen gerekebilir.
  // Şimdilik tek bir batch ile yapıyoruz.
  const batch = db.batch();
  let updatedCount = 0;

  snapshot.forEach((doc) => {
    const postData = doc.data();
    const updates = {}; // Bu döküman için yapılacak güncellemeleri tutan obje

    // --- ESKİ'den YENİ'ye DÖNÜŞÜM MANTIĞI BURADA ---

    // 1. downloadurl -> mediaUrls dönüşümü
    if (postData.downloadurl && !postData.mediaUrls) {
      updates.mediaUrls = [postData.downloadurl]; // Tek URL'yi bir diziye koy
      updates.mediaType = "image"; // Varsayılan olarak image diyelim
    }
    
    // 2. Eksik 'authorId' alanı için varsayılan atama
    if (!postData.authorId) {
      // Bu alanı manuel olarak doldurman gerekebilir, şimdilik boş bırakıyoruz
      // veya bir varsayılan değer atayabiliriz.
      updates.authorId = "ysBHhImH3yR0meZYqCRsEHdoDv63";
    }

    // 3. Eksik 'visibility' alanı için varsayılan atama
    if (!postData.visibility) {
      updates.visibility = "public";
    }
    
    // 4. Eksik 'visibleTo' alanı için varsayılan atama
     if (!postData.visibleTo) {
      updates.visibleTo = [];
    }

    // 5. isLiked -> likedBy dönüşümü
    if (postData.isLiked !== undefined && !postData.likedBy) {
       updates.likedBy = []; // Başlangıçta boş bir dizi
    }

    // 6. Eksik 'commentCount' alanı
    if (postData.commentCount === undefined) {
        updates.commentCount = 0;
    }


    // Eğer bu döküman için en az bir güncelleme varsa, batch'e ekle
    if (Object.keys(updates).length > 0) {
      batch.update(doc.ref, updates);
      updatedCount++;

      // İsteğe bağlı: Eski ve artık kullanılmayan alanları silebiliriz
      // batch.update(doc.ref, {
      //   downloadurl: admin.firestore.FieldValue.delete(),
      //   isLiked: admin.firestore.FieldValue.delete(),
      // });
    }
  });

  // Tüm güncellemeleri tek bir işlemde Firestore'a gönder
  await batch.commit();

  const resultMessage = `${updatedCount} adet post dökümanı başarıyla güncellendi.`;
  console.log(resultMessage);
  response.status(200).send({message: resultMessage}); 
  return {message: resultMessage};
});