import { onDocumentCreated } from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";
import * as logger from "firebase-functions/logger";

admin.initializeApp();

/**
 * Trigger, który aktualizuje średnią ocenę i łączną liczbę ocen w kolekcji remoteRoutes
 * po dodaniu nowej oceny do kolekcji routeRatings.
 */
export const calculateAverageRating = onDocumentCreated("routeRatings/{ratingId}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) {
    logger.info("Brak danych w evencie");
    return;
  }
  const ratingData = snapshot.data();
  const routeId = ratingData.routeId;
  const newRating = ratingData.rating;
  if (!routeId || typeof newRating !== "number") {
    logger.error("Nieprawidłowe dane oceny:", ratingData);
    return;
  }
  const db = admin.firestore();
  const routeRef = db.collection("remoteRoutes").doc(routeId);
  try {
    await db.runTransaction(async (transaction) => {
      const routeDoc = await transaction.get(routeRef);
      if (!routeDoc.exists) {
        logger.warn(`Trasa o ID ${routeId} nie istnieje.`);
        return;
      }
      const data = routeDoc.data();
      const oldAverage = data?.averageRating || 0;
      const oldTotal = data?.totalRatings || 0;
      const newTotal = oldTotal + 1;
      const newAverage = (oldAverage * oldTotal + newRating) / newTotal;
      transaction.update(routeRef, {
        averageRating: newAverage,
        totalRatings: newTotal,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
      logger.info(`Zaktualizowano trasę ${routeId}: Nowa średnia = ${newAverage}, Liczba ocen = ${newTotal}`);
    });
  } catch (error) {
    logger.error("Błąd podczas aktualizacji średniej oceny:", error);
  }
});
