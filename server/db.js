import mysql from 'mysql2/promise';
import pg from 'pg';
import dotenv from 'dotenv';
dotenv.config();

let pool;
const isPostgres = !!process.env.DATABASE_URL;

if (isPostgres) {
  const { Pool } = pg;
  pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: {
      rejectUnauthorized: false
    }
  });
} else {
  pool = mysql.createPool({
    host: process.env.DB_HOST || '127.0.0.1',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_NAME || 'campus_events',
    multipleStatements: true,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0,
    connectTimeout: 10000 // Increase timeout to 10s
  });
}

// Compatibility wrapper
export default {
  query: async (text, params) => {
    if (isPostgres) {
      let count = 0;
      let queryText = text.replace(/\?/g, () => `$${++count}`);

      // Handle PostgreSQL returning for INSERTs
      const isInsert = queryText.trim().toUpperCase().startsWith('INSERT');
      if (isInsert && !queryText.toUpperCase().includes('RETURNING')) {
        queryText += ' RETURNING id';
      }

      const result = await pool.query(queryText, params);

      // Add insertId compatibility for PostgreSQL
      if (isInsert && result.rows.length > 0) {
        result.insertId = result.rows[0].id;
      }

      return [result.rows, result];
    } else {
      // MySQL
      try {
        const [rows, fields] = await pool.execute(text, params);
        return [rows, fields];
      } catch (err) {
        console.error('MySQL Error:', err.message, '| Query:', text);
        throw err;
      }
    }
  },
  execute: async (text, params) => {
    if (isPostgres) {
      let count = 0;
      let queryText = text.replace(/\?/g, () => `$${++count}`);

      const isInsert = queryText.trim().toUpperCase().startsWith('INSERT');
      if (isInsert && !queryText.toUpperCase().includes('RETURNING')) {
        queryText += ' RETURNING id';
      }

      const result = await pool.query(queryText, params);

      if (isInsert && result.rows.length > 0) {
        result.insertId = result.rows[0].id;
      }

      return [result.rows, result];
    } else {
      // MySQL
      try {
        const [rows, fields] = await pool.execute(text, params);
        return [rows, fields];
      } catch (err) {
        console.error('MySQL Error:', err.message, '| Query:', text);
        throw err;
      }
    }
  },
  pool: pool,
  isPostgres: isPostgres
};
