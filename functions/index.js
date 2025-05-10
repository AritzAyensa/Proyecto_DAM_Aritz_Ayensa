const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const admin = require('firebase-admin');
const { getMessaging } = require('firebase-admin/messaging');

admin.initializeApp();
const messaging = getMessaging();

exports.sendNotification = onDocumentCreated(
  {
    document: 'notificacionesEmergentes/{notifId}',
    region: 'europe-west1',
  },
  async (event) => {
    try {
      const data = event.data?.data();
      if (!data) return null;

      const {
        toUid,     
        fromUid,   
        fromName,
        listName,  
        tipo    
      } = data;

      if (!toUid || !tipo) return null;

      // 1) Tokens del usuario destino
      const userDoc = await admin
        .firestore()
        .collection('usuarios')
        .doc(toUid)
        .get();
      if (!userDoc.exists) return null;

      const tokens = userDoc.get('fcmTokens') || [];
      if (tokens.length === 0) return null;

      // 2) Configurar título, cuerpo y canal según tipo
      let title, body, channel;
      if (tipo === 1) {
        title   = 'Nueva invitación';
        body    = `${fromName} te ha compartido "${listName}"`;
        channel = 'notificacionesEmergentes';
      } else if (tipo === 2) {
        title   = 'Compra completada';
        body    = `${fromName} ha completado la compra "${listName}"`;
        channel = 'notificacionesEmergentes';
      } else {
        console.warn(`Tipo desconocido: ${tipo}`);
        return null;
      }

      // 3) Enviar push a cada token
      const sendPromises = tokens.map(token => {
        const msg = {
          token,
          notification: { title, body },
          data: {
            tipo:     String(tipo),
            fromUid,
            fromName,
            listName
          },
          android: {
            priority: 'HIGH',
            notification: {
              channelId: channel,
              icon:      'ic_logo' 
            }
          }
        };
        return messaging.send(msg)
          .then(() => ({ success: true, token }))
          .catch(error => ({ success: false, token, error }));
      });

      const results      = await Promise.all(sendPromises);
      const successCount = results.filter(r => r.success).length;
      const failed       = results.filter(r => !r.success);

      console.log(`${successCount} notificaciones enviadas (tipo ${tipo})`);

      // 4) Limpiar tokens inválidos
      if (failed.length) {
        const badTokens = failed.map(f => f.token);
        await admin
          .firestore()
          .collection('usuarios')
          .doc(toUid)
          .update({
            fcmTokens: admin.firestore.FieldValue.arrayRemove(...badTokens)
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