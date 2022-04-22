package savestring.demo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) throws SQLException, FileNotFoundException, IOException  {
		
        SpringApplication.run(DemoApplication.class, args);	
	}

}
