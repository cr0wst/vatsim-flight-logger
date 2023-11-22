// setup winston
import { createLogger, format, transports } from "winston";
import config from "./config";

const logger = createLogger({
  level: config.LOG_LEVEL || "info",
  format: format.combine(
    format.colorize(),
    format.timestamp(),
    format.align(),
    format.printf(info => `${info.timestamp} [${info.level}] ${info.message}`)
  ),
  transports: [
    new transports.Console()]
});

export default logger;
