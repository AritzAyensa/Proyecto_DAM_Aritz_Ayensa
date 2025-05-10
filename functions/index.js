const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const admin = require('firebase-admin');
const { getMessaging } = require('firebase-admin/messaging');

admin.initializeApp();
const messaging = getMessaging();

exports.sendInviteNotification = onDocumentCreated(
  {
    document: 'invitations/{inviteId}',
    region: 'europe-west1',
  },
  async (event) => {
    try {
      const invite = event.data?.data();
      if (!invite) return null;

      const userDoc = await admin
        .firestore()
        .collection('usuarios')
        .doc(invite.toUid)
        .get();
      if (!userDoc.exists) return null;

      const tokens = userDoc.get('fcmTokens') || [];
      if (tokens.length === 0) return null;

      const sendPromises = tokens.map(token => {
        const msg = {
          token,
          notification: {
            title: 'Nueva invitación',
            body: `${invite.fromName} te ha compartido "${invite.listName}"`,
          },
          data: {
            listId: invite.listId || '',
            fromUid: invite.fromUid || '',
          },
          android: {
            priority: 'HIGH',
            notification: { channelId: 'invitaciones' },
          },
        };
        return messaging.send(msg)
          .then(() => ({ success: true, token }))
          .catch(err => ({ success: false, token, error: err }));
      });

      const results = await Promise.all(sendPromises);
      const successCount = results.filter(r => r.success).length;
      const failed = results.filter(r => !r.success);

      console.log(`${successCount} notificaciones enviadas`);

      if (failed.length) {
        const badTokens = failed.map(f => f.token);
        await admin
          .firestore()
          .collection('usuarios')
          .doc(invite.toUid)
          .update({
            fcmTokens: admin.firestore.FieldValue.arrayRemove(...badTokens),
          });
        console.log(`Tokens eliminados: ${badTokens.join(', ')}`);
      }

      return null;
    } catch (e) {
      console.error('Error crítico:', e);
      return null;
    }
  }
);