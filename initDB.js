import mysql from 'mysql2/promise';
import fs from 'fs';
import path from 'path';
import dotenv from 'dotenv';
dotenv.config();

async function initDB() {
    try {
        const connection = await mysql.createConnection({
            host: process.env.DB_HOST || 'localhost',
            user: process.env.DB_USER || 'root',
            password: process.env.DB_PASSWORD || '',
            multipleStatements: true
        });

        const schema = fs.readFileSync('database/schema.sql', 'utf8');
        await connection.query(schema);
        console.log('Database schema imported successfully!');
        await connection.end();
    } catch (err) {
        console.error('Failed to import schema:', err.message);
    }
}

initDB();
