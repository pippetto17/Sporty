package testing;

import model.dao.ConnectionFactory;
import model.dao.ProjectDAO;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreateProjectTest {

    private Connection connection;
    private ProjectDAO projectDAO;
    private final String testProjectName = "Test Project";

    @BeforeAll
    public void setupDatabase() throws SQLException {
        connection = ConnectionFactory.getConnection();
        projectDAO = new ProjectDAO(connection);
    }

    @AfterAll
    public void cleanupDatabase() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM Project WHERE name = ?")) {
            stmt.setString(1, testProjectName);
            stmt.executeUpdate();
        }

        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void testCreateNewProject() throws SQLException {
        String testProjectDescription = "This is a test project.";
        projectDAO.addProject(testProjectName, testProjectDescription);

        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM Project WHERE name = ?")) {
            stmt.setString(1, testProjectName);
            var rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            assertEquals(1, count, "Il progetto dovrebbe essere stato aggiunto.");
        }
    }
}
