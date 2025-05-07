const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendInviteNotification = onDocumentCreated(
  {
    document: 'invitations/{inviteId}',
    region: 'europe-west1',
  },
  async (event) => {
    const invite = event.data?.data();
    if (!invite) return null;

    // Leer todos los tokens del usuario (array)
    const userSnap = await admin
      .firestore()
      .collection('users')
      .doc(invite.toUid)
      .get();
    const tokens = userSnap.get('fcmTokens');
    if (!tokens || tokens.length === 0) return null;

    const payload = {
      notification: {
        title: 'Nueva invitación',
        body: `${invite.fromName} te ha compartido "${invite.listName}"`,
      },
    };

    // Enviar a todos los tokens
    return admin.messaging().sendToDevice(tokens, payload);
  }
);
