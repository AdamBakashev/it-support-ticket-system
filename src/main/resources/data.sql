-- Realistische Testdaten für Entwicklung und Demonstration
-- Passwort für alle Testnutzer: password123 (BCrypt Hash: $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a)

-- 1. Benutzer (Admin, Support Agents, Mitarbeiter)
INSERT INTO users (id, username, email, password_hash, role, department, is_active, created_at) VALUES
(1, 'admin', 'admin@company.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'ADMIN', 'IT Operations', true, CURRENT_TIMESTAMP),
(2, 'agent.meyer', 'meyer@company.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'SUPPORT_AGENT', '1st Level Support', true, CURRENT_TIMESTAMP),
(3, 'agent.weber', 'weber@company.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'SUPPORT_AGENT', '2nd Level Support', true, CURRENT_TIMESTAMP),
(4, 'dev.schmidt', 'schmidt@company.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'EMPLOYEE', 'Software Development', true, CURRENT_TIMESTAMP),
(5, 'hr.fischer', 'fischer@company.de', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'EMPLOYEE', 'Human Resources', true, CURRENT_TIMESTAMP);

-- 2. Kategorien
INSERT INTO categories (id, name, description) VALUES
(1, 'Hardware', 'Defekte Geräte, Monitore, Tastaturen, Dockingstationen und Zubehör'),
(2, 'Software & Lizenzen', 'Software-Installationen, Lizenz-Anfragen und Programmfehler'),
(3, 'Netzwerk & VPN', 'WLAN, VPN-Tunnel ins Firmennetz, DNS- und Verbindungsabbrüche'),
(4, 'Zugriff & Berechtigungen', 'GitLab-, Jira-, Server- und Datenbank-Zugriffsrechte');

-- 3. Tickets
INSERT INTO tickets (id, title, description, status, priority, category_id, created_by_id, assigned_to_id, created_at, updated_at, resolved_at) VALUES
(1, 'VPN-Verbindung trennt sich stündlich im Homeoffice', 'Seit dem letzten macOS Update bricht die OpenVPN-Verbindung exakt alle 60 Minuten ab. Reconnect schlägt zunächst fehl.', 'IN_PROGRESS', 'HIGH', 3, 4, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(2, 'Defekter USB-C Port an Dockingstation Arbeitsplatz 3.14', 'Der linke externe Monitor erhält kein Signal mehr. Kabel wurde bereits getauscht, Fehler liegt an der Dockingstation.', 'OPEN', 'MEDIUM', 1, 5, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(3, 'GitLab Runner Maintainer-Rechte für payment-service', 'Für das CI/CD Pipeline Deployment im neuen Payment-Service Projekt fehlen die Maintainer-Rechte auf Repository-Ebene.', 'RESOLVED', 'HIGH', 4, 4, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'IntelliJ IDEA Ultimate Lizenzverlängerung', 'Die Jahreslizenz für IntelliJ läuft in 3 Tagen ab. Bitte um Zuweisung des neuen Lizenzschlüssels über das JetBrains Dashboard.', 'CLOSED', 'LOW', 2, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 4. Kommentare
INSERT INTO ticket_comments (id, ticket_id, author_id, comment_text, is_internal, created_at) VALUES
(1, 1, 3, 'Client-Logs analysiert: Möglicherweise greift das DHCP Lease Timeout im Heimrouter. Bitte IPv6 in den VPN-Einstellungen deaktivieren.', false, CURRENT_TIMESTAMP),
(2, 1, 4, 'Habe IPv6 deaktiviert, teste es nun für 2 Stunden im Produktivbetrieb.', false, CURRENT_TIMESTAMP),
(3, 3, 2, 'Maintainer-Rolle in der Gruppe "Core Services" wurde zugewiesen.', false, CURRENT_TIMESTAMP);

-- 5. Audit Log Einträge
INSERT INTO ticket_audit_logs (id, ticket_id, changed_by_id, old_status, new_status, change_summary, created_at) VALUES
(1, 1, 3, 'OPEN', 'IN_PROGRESS', 'Ticket an agent.weber zugewiesen und Status auf IN_PROGRESS gesetzt', CURRENT_TIMESTAMP),
(2, 3, 2, 'OPEN', 'IN_PROGRESS', 'In Bearbeitung genommen', CURRENT_TIMESTAMP),
(3, 3, 2, 'IN_PROGRESS', 'RESOLVED', 'GitLab-Rechte erfolgreich vergeben', CURRENT_TIMESTAMP),
(4, 4, 1, 'RESOLVED', 'CLOSED', 'Lizenzverlängerung bestätigt und Ticket geschlossen', CURRENT_TIMESTAMP);

-- Sequence Restart: Setzt Auto-Identity auf ID 10+, damit neue Inserts nicht mit Seed-IDs kollidieren
ALTER TABLE users ALTER COLUMN id RESTART WITH 10;
ALTER TABLE categories ALTER COLUMN id RESTART WITH 10;
ALTER TABLE tickets ALTER COLUMN id RESTART WITH 10;
ALTER TABLE ticket_comments ALTER COLUMN id RESTART WITH 10;
ALTER TABLE ticket_audit_logs ALTER COLUMN id RESTART WITH 10;

