package utils;

public class Logging {

	public static void log(String logLevel, String message) {
		try {
			switch (logLevel) {
			case "info":
				Helper.logger.info("LogMessage={}", message);
				break;
			case "debug":
				Helper.logger.debug("LogMessage={}", message);
				break;
			case "error":
				Helper.logger.error("LogMessage={}", message);

				break;
			default:
				Helper.logger.info("LogMessage={}", message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void log(String message) {
		log("info", message);
	}

}
