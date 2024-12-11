package pt.isec.pd.spring_boot.exemplo3.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.pd.spring_boot.exemplo3.models.UserInfo;
import pt.isec.pd.spring_boot.exemplo3.server.ManageDB;

@RestController
public class RegisterController {

    @PostMapping("/register")
    public ResponseEntity registerUser(@RequestBody UserInfo user) {
        // Validate the incoming user information
        if (user.getName() == null || user.getEmail() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body("Name, email, and password are mandatory.");
        }

        ManageDB db = new ManageDB();
        db.insertUtl(user.getName(), user.getId(),user.getEmail(),user.getPassword());
        db.closeConnection();
        return ResponseEntity.ok(HttpStatus.OK);
    }
}