package schoolLibrary.borrowBook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

public class BorrowBookController {
	
private Connection connection;
	
	@FXML
	private TextField memberIdTxtfield;
	@FXML
	private TextField isbnTxtfield;
	
	@FXML
	private Label nameLbl;
	@FXML
	private Label bookLbl;
	@FXML
	private Label dueDateLbl;
	
	@FXML
	private Button borrowBtn;

	private static long DAY_IN_MS = 1000 * 60 * 60 * 24;
	
	/**
     * Initializes the controller class. 
     * This method is automatically called after the fxml file has been loaded.
	 * @throws SQLException 
     */
    @FXML
    private void initialize() throws SQLException {
    	// load the sqlite-JDBC driver using the current class loader
    	try {
    		Class.forName( "org.sqlite.JDBC" );
    	} catch (ClassNotFoundException e1) {
    	}
    	        
        // Whenever the user types in something, update
        // Registering event listener
        // obs is query.textProperty()
        // oldText and newText are old and new values of query.getText()
    	memberIdTxtfield.textProperty().addListener(      (obs, oldText, newText) -> {updateName();}    );
    	isbnTxtfield.textProperty().addListener(      (obs, oldText, newText) -> { updateBook(); }    );
        
    	borrowBtn.setOnAction((e)-> { clickBorrow(); } );
    	
        // connect to the database
        try {
			connection = DriverManager.getConnection( "jdbc:sqlite:SchoolLibrarySystem.sqlite");
		} catch (SQLException e) {
			handleError(e);
		}

        // update initially shows everything, like in iTunes
        updateName();
        updateBook();
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(System.currentTimeMillis() + (7 * DAY_IN_MS));
        dueDateLbl.setText(dateFormat.format(date));
    }
    
    /**
     * this method is called when the user changes the TextField's text
     */
	@FXML
	private void updateName() {
		try {
			// The parameter comes from the query TextField
			// at first, it is empty
			nameLbl.setText("");
			String param = memberIdTxtfield.getText();
						
			// generate parameterized sql
			String sql = "SELECT Member.FirstName AS fName, Member.LastName AS lName, Student.Major AS major, Faculty.Department As dept" + 
							" FROM Member" +
							" LEFT JOIN Student USING (memberId)" +
							" LEFT JOIN Faculty USING (memberId)" +
							" WHERE Member.MemberId = ?;";

			// prepared statement
			PreparedStatement stmt = connection.prepareStatement( sql );
			
			stmt.setString( 1, param.trim().replaceAll("[^\\d]", ""));
			
			// get results
			ResultSet res = stmt.executeQuery();
			if ( res.next() ) {
				String memberInfo = "";
				if (res.getString("major")==null)
					memberInfo += String.format("[Faculty - %s] ", res.getString("dept"));
				else
					memberInfo += String.format("[Student - %s] ", res.getString("major"));
				
				memberInfo += res.getString("fName") + " " + res.getString("lName");
				
				nameLbl.setText(memberInfo);
			}

		} catch (SQLException e) {
			handleError(e);
		}
	}
	
	@FXML
	private void updateBook() {
		try {
			// The parameter comes from the query TextField
			// at first, it is empty
			bookLbl.setText("");
			String param = isbnTxtfield.getText();
						
			// generate parameterized sql
			String sql = "SELECT Book.ISBN AS book_isbn, Book.Title AS book_title, Book.Author AS book_author" +
					" FROM Book" +
					" WHERE Book.ISBN = ?;";

			// prepared statement
			PreparedStatement stmt = connection.prepareStatement( sql );
			
			stmt.setString( 1, param.trim().replaceAll("[^\\d]", ""));
			
			// get results
			ResultSet res = stmt.executeQuery();
			if ( res.next() ) {
				bookLbl.setText(res.getString("book_title") + " by " + res.getString("book_author"));
			}

		} catch (SQLException e) {
			handleError(e);
		}
	}
	
	@FXML
	private void clickBorrow() {
		try {
			
			String paramMemId = memberIdTxtfield.getText();
			String paramIsbn = isbnTxtfield.getText();
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date currDate = new Date();
	        Date dueDate = new Date(System.currentTimeMillis() + (7 * DAY_IN_MS));
			
			// generate parameterized sql
			String sql = "INSERT INTO Borrow (ISBN, MemberId, BorrowDate, DueDate, ReturnedDate, Rating)" +
							" VALUES" +
							" (?, ?, "+ dateFormat.format(currDate) + "," + dateFormat.format(dueDate) +", NULL, NULL);";
	
			// prepared statement
			PreparedStatement stmt = connection.prepareStatement( sql );
			
			stmt.setString( 1, paramIsbn.trim().replaceAll("[^\\d]", ""));
			stmt.setString( 2, paramMemId.trim().replaceAll("[^\\d]", ""));
			
			// get results
			ResultSet res = stmt.executeQuery();
			
		
		} catch (SQLException e) {
			handleError(e);
		}
		
	}

	
	private void handleError(Exception e) {
		// Alert the user when things go terribly wrong
		Alert alert = new Alert(AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
		// Close the program when the user clicks close on the alert
		alert.setOnCloseRequest(event -> Platform.exit());
		// show the alert
		alert.show();
	}

}