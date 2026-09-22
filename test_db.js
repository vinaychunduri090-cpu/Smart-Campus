import mysql from 'mysql2/promise';

async function testConnection() {
    try {
        console.log('Attempting to connect to MySQL on 127.0.0.1...');
        const connection = await mysql.createConnection({
            host: '127.0.0.1',
            user: 'root',
            password: '',
            connectTimeout: 5000
        });
        console.log('Connected successfully to 127.0.0.1!');
        await connection.end();
    } catch (err) {
        console.error('Failed to connect to 127.0.0.1:', err.message);
    }
}

testConnection();
