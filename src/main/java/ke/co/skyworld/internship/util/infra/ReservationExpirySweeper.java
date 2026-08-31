package ke.co.skyworld.internship.util.infra;


import ke.co.skyworld.internship.repository.ReservationExpiryRepository;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Rule 2's expiry-handling side: sweeps stock_reservations for anything
 * active past its expires_at, and releases it via the existing
 * release_reservation() function - which correctly re-derives available
 * stock from the ledger, no double-crediting.
 * <p>
 * Registered as a Quartz cron job via SkyCoreScheduler in Main.bootstrap() -
 * this class only holds the sweep logic, not the scheduling wiring, so it
 * can be unit tested independently of Quartz.
 */
public class ReservationExpirySweeper {

    private static final String SWEEPER_IDENTITY = "reservation-expiry-sweeper";

    private final ReservationExpiryRepository reservationExpiryRepository = new ReservationExpiryRepository();

    public void sweep(Map<String, Object> jobData) {
        List<Long> expiredIds;
        try {
            expiredIds = reservationExpiryRepository.findExpiredActiveReservationIds();
        } catch (SQLException e) {
            Log.error(getClass(), "sweep", "Failed to query expired reservations: " + e.getMessage(), e);
            return;
        }

        if (expiredIds.isEmpty()) {
            return; // quiet on the common case - don't log noise every minute
        }

        int released = 0;
        for (Long reservationId : expiredIds) {
            try {
                if (reservationExpiryRepository.releaseReservation(reservationId, SWEEPER_IDENTITY)) {
                    released++;
                }
            } catch (SQLException e) {
                // One bad reservation shouldn't stop the rest of the sweep -
                // log and continue rather than aborting the whole batch.
                Log.error(getClass(), "sweep",
                        "Failed to release expired reservation " + reservationId + ": " + e.getMessage(), e);
            }
        }

        Log.info(getClass(), "sweep", "Released " + released + " of " + expiredIds.size() + " expired reservations");
    }
}
