package dev.armadeus.core.util;

import com.nachtraben.pineappleslice.redis.Redis;
import com.nachtraben.pineappleslice.redis.RedisModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

public class RedisUtil {

    private static final Logger log = LogManager.getLogger();

    private static RedisModule module;

    public static boolean isEnabled() {
        return module != null;
    }

    public static <T> T runQuery(Function<Redis, T> query) {
        try (Redis r = module.getProvider().getSession(14)) {
            return query.apply(r);
        } catch (Exception e) {
            log.error("Failed to execute redis query, database down?", e);
        }
        return null;
    }

    public static <T> T runLegacyQuery(int db, Function<Redis, T> query) {
        try (Redis r = module.getProvider().getSession(db)) {
            return query.apply(r);
        } catch (Exception e) {
            log.error("Failed to execute redis query, database down?", e);
        }
        return null;
    }


    public static void setModule(RedisModule module) {
        RedisUtil.module = module;
    }
}