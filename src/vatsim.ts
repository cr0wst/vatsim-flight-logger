import axios from "axios";
import config from "./config";

export async function fetchData(): Promise<VatsimData> {
  const response = await axios.get(config.VATSIM_DATA_ENDPOINT);
  return await response.data;
}

/**
 * This is the data returned from the VATSIM Data endpoint.
 */
type VatsimData = {
  pilots: {
    cid: number,
    name: string,
    callsign: string,
    server: string,
    latitude: number,
    longitude: number,
    altitude: number,
    groundspeed: number,
    heading: number,
    flight_plan: {
      departure: string,
      arrival: string,
    }
    last_updated: string,
  }[]
}