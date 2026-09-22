import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import db from './db.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export async function initializeDatabase() {
    try {
        let exists = false;

        if (db.isPostgres) {
            const [rows] = await db.query(`
                SELECT EXISTS (
                    SELECT FROM information_schema.tables 
                    WHERE table_schema = 'public' 
                    AND table_name = 'users'
                );
            `);
            exists = rows[0].exists;
        } else {
            // MySQL check
            const [rows] = await db.query("SHOW TABLES LIKE 'users'");
            exists = rows.length > 0;
        }

        if (!exists) {
            console.log(`${db.isPostgres ? 'PostgreSQL' : 'MySQL'} tables not found. Initializing schema...`);
            const schemaFile = db.isPostgres ? '../database/schema_pg.sql' : '../database/schema.sql';
            const schemaPath = path.join(__dirname, schemaFile);
            const schema = fs.readFileSync(schemaPath, 'utf8');

            // Execute schema
            await db.query(schema);
            console.log('Database schema initialized successfully!');
        } else {
            console.log('Database tables already exist.');
        }
    } catch (err) {
        console.error('Database initialization failed:', err.message);
    }
}
