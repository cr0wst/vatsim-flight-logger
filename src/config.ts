import dotenv from "dotenv";
import { toNumber } from "lodash";

dotenv.config();
export default {
  VATSIM_DATA_ENDPOINT: process.env.VATSIM_DATA_ENDPOINT!!,
  SCRAPE_INTERVAL_MS: toNumber(process.env.SCRAPE_INTERVAL_MS!!),
  LOG_LEVEL: process.env.LOG_LEVEL!!,
  DB_CONNECTION_STRING: process.env.DB_CONNECTION_STRING!!,
  DISCORD_BOT_TOKEN: process.env.DISCORD_BOT_TOKEN!!,
};