import { onDocumentCreated, onDocumentUpdated, onDocumentDeleted } from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";
import * as logger from "firebase-functions/logger";

admin.initializeApp();

async function updateAverageRating(routeId: string) {
  const db = admin.firestore();
  const ratingsSnapshot = await db.collection("routeRatings").where("routeId", "==", routeId).get();

  let totalRatingValue = 0;
  const ratingCount = ratingsSnapshot.size;

  if (ratingCount === 0) {
    await db.collection("remoteRoutes").doc(routeId).update({
      averageRating: 0,
      totalRatings: 0,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    return;
  }

  ratingsSnapshot.forEach((doc) => {
    totalRatingValue += doc.data().rating;
  });

  const averageRating = totalRatingValue / ratingCount;

  await db.collection("remoteRoutes").doc(routeId).update({
    averageRating: averageRating,
    totalRatings: ratingCount,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  logger.info(`Updated route ${routeId}: Avg = ${averageRating}, Total = ${ratingCount}`);
}

export const onRatingCreated = onDocumentCreated("routeRatings/{ratingId}", async (event) => {
  const ratingData = event.data?.data();
  const routeId = ratingData?.routeId;
  if (routeId) await updateAverageRating(routeId);
});

export const onRatingUpdated = onDocumentUpdated("routeRatings/{ratingId}", async (event) => {
  const ratingData = event.data?.after.data();
  const routeId = ratingData?.routeId;
  if (routeId) await updateAverageRating(routeId);
});

export const onRatingDeleted = onDocumentDeleted("routeRatings/{ratingId}", async (event) => {
  const ratingData = event.data?.data();
  const routeId = ratingData?.routeId;
  if (routeId) await updateAverageRating(routeId);
});
