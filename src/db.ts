import knex from 'knex';
import config from "./config";

export const db = knex({
  client: 'pg',
  connection: config.DB_CONNECTION_STRING,
  searchPath: ['knex', 'public'],
})

export type DiscordChannel = {
  id: string,
  config: {
    filters: {
      property: string,
      value: string
    }[]
  }
}

export type Controller = {
  batch: string,
  cid: number,
  name: string,
  callsign: string,
  frequency: string,
  facility: number,
  rating: number,
  server: string,
  visual_range: number,
  text_atis: string[],
  last_updated: string,
  logon_time: string
}