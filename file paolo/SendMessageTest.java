package testing;

import model.dao.ConnectionFactory;
import model.dao.MessageDAO;
import model.domain.Message;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SendMessageTest {

    private Connection connection;
    private MessageDAO messageDAO;
    private final long conversationID = 1L;
    private final String sender = "testUser";
    private final LocalDateTime fakeTimestamp = LocalDateTime.of(2023, 1, 1, 12, 0, 0);

    @BeforeAll
    public void setupDatabase() throws SQLException {
        connection = ConnectionFactory.getConnection();
        messageDAO = new MessageDAO(connection);

        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO User (username, password, name, surname, role) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE username=username")) {
            stmt.setString(1, sender);
            stmt.setString(2, "testPassword");
            stmt.setString(3, "Test User");
            stmt.setString(4, "Test Surname");
            stmt.setInt(5, 1);
            stmt.executeUpdate();
        }

        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO Conversation (id, description, projectName) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE id=id")) {
            stmt.setLong(1, conversationID);
            stmt.setString(2, "Test Conversation");
            stmt.setString(3, "Test Project");
            stmt.executeUpdate();
        }
    }

    @AfterAll
    public void cleanupDatabase() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM Message WHERE conversationID = ? AND senderUsername = ?")) {
            stmt.setLong(1, conversationID);
            stmt.setString(2, sender);
            stmt.executeUpdate();
        }

        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM User WHERE username = ?")) {
            stmt.setString(1, sender);
            stmt.executeUpdate();
        }

        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void testSendMessage() throws SQLException {
        String messageText = "Hello, this is a test message.";
        Message messageBean = new Message(conversationID, sender, messageText, fakeTimestamp);

        messageDAO.addMessage(messageBean);

        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM Message WHERE conversationID = ? AND senderUsername = ?")) {
            stmt.setLong(1, conversationID);
            stmt.setString(2, sender);
            var rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            assertEquals(1, count, "Il messaggio dovrebbe essere stato inviato.");
        }
    }
}
