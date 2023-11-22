import log from "./log";
import { db } from "./db";
import config from "./config";
import { fetchData } from "./vatsim";

async function main() {
  log.info("Starting up...");

  // Run once on startup
  await run();

  // call run every configured interval
  await setInterval(run, config.SCRAPE_INTERVAL_MS);
}

async function run() {
  log.info("Fetching Pilot Data");
  const data = await fetchData();
  await db("flights").insert(
    data.pilots.filter((pilot) => pilot.flight_plan?.departure && pilot.flight_plan?.arrival)
      .map((pilot) => ({
        callsign: pilot.callsign,
        cid: pilot.cid,
        name: pilot.name,
        latitude: pilot.latitude,
        longitude: pilot.longitude,
        altitude: pilot.altitude,
        groundspeed: pilot.groundspeed,
        departure: pilot.flight_plan.departure,
        arrival: pilot.flight_plan.arrival
      })));

  log.info("Fetching Calculated Arrivals");
  const arrivals = await db("flight_status").select("*").where("aircraft_status", "Arrived");

  if (arrivals.length == 0) {
    log.info("No arrivals found");
  } else {

    log.info("Fetching ARTCC Units from DB");
    const units = await db("units").select("*");


    for (const arrival of arrivals) {
      const source = units.find((unit) => unit.artcc === arrival.departure_artcc);
      const destination = units.find((unit) => unit.artcc === arrival.arrival_artcc);

      if (source && destination) {
        const color = calculateColor(source);
        if (color !== "gray" && source[color] > 0) {
          log.info(`Moving units from ${source.artcc} to ${destination.artcc}`);
          destination[color]++;
          source[color]--;
        } else {
          log.info(`No units to move from ${source.artcc} to ${destination.artcc}`);
        }


        // Insert the processed arival into the flight archive table
        log.info("Inserting processed arrivals into flight archive");
        await db("flights_archive").insert({
          callsign: arrival.callsign,
          cid: arrival.cid,
          name: arrival.name,
          departure: arrival.departure,
          arrival: arrival.arrival,
          status: "Processed"
        });
      } else {
        log.info(`Can't resolve source and destination for ${arrival.departure_artcc} to ${arrival.arrival_artcc}`);
      }
    }

    // Commit the changes to the database
    log.info("Committing changes to DB");
    units.forEach(async (unit) => {
      await db("units").update(unit).where("artcc", unit.artcc);
    });
  }
  await db("flights").del();
}

function calculateColor(units: any) {
  const red = units.red;
  const green = units.green;
  const blue = units.blue;

  if (red > green && red > blue) {
    return "red";
  } else if (green > red && green > blue) {
    return "green";
  } else if (blue > red && blue > green) {
    return "blue";
  } else {
    return "gray";
  }
}

main().catch((err) => {
  log.error(err);
  process.exit(1);
});