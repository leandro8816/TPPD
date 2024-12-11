package pt.isec.pd.spring_boot.exemplo3.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pt.isec.pd.spring_boot.exemplo3.server.ManageDB;
import pt.isec.pd.spring_boot.exemplo3.utils.CodigoRegisto;
import pt.isec.pd.spring_boot.exemplo3.models.Event;

@RestController
@RequestMapping("list")
public class ListController {

    @GetMapping("/isAdmin")
    public boolean isAdmin(Authentication authentication){
        Jwt userDetails = (Jwt) authentication.getPrincipal();
        String scope = userDetails.getClaim("scope");
        return "ADMIN".equals(scope);
    }

    @PostMapping("/addEvent")
    public ResponseEntity addEvent(@RequestBody Event event, Authentication authentication) {
        if(!isAdmin(authentication))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized. Admin privileges required.");
        if (event.getName() == null || event.getStartingHour() == null || event.getFinishingHour() == null || event.getPlace() == null || event.getDate() == null) {
            return ResponseEntity.badRequest().body("Name, local, and date are mandatory.");
        }

        ManageDB db = new ManageDB();
        boolean result = db.insertEvent(event.getName(),event.getPlace(),event.getDate(),event.getStartingHour(),event.getFinishingHour());
        db.closeConnection();
        if(result)
            return ResponseEntity.ok(HttpStatus.OK);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Cannot add event");
    }
    @PostMapping("/getAttendanceClient")
    public ResponseEntity<String> getAttendanceClient(Authentication authentication) {
        String userEmail = authentication.getName();

        ManageDB db = new ManageDB();
        String result  = db.get3cli(userEmail);
        db.closeConnection();
        if(result != null){
            return ResponseEntity.ok(result);
        }else{
            return ResponseEntity.badRequest().body(null);
        }

    }
    @DeleteMapping("/deleteEvent")
    public ResponseEntity deleteEvent(@RequestBody String name, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized. Admin privileges required.");
        }
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body("Name is required!");
        }
        ManageDB db = new ManageDB();
        if (db.deleteEventBool(name)) {
            db.closeConnection();
            return ResponseEntity.ok(HttpStatus.OK);
        }
        db.closeConnection();
        return ResponseEntity.badRequest().body("Event not found");
    }

    @PostMapping("/submitCode")
    public ResponseEntity<String> submitCode(@RequestBody String code,Authentication authentication) {

        ManageDB db = new ManageDB();
        String userEmail = authentication.getName();
        int intCode = Integer.parseInt(code);

        if(db.codeExists(intCode)){
            db.insertAssiste(userEmail,db.getEventNameByCode(intCode));
            return ResponseEntity.ok("Código submetido com sucesso");
        }else{
            return ResponseEntity.badRequest().body("Codigo não encontrado");
        }

    }
    @PostMapping("/getAllEvents")
    public ResponseEntity<String> getAllEvents(Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized. Admin privileges required.");
        }
        ManageDB db = new ManageDB();
        String result = db.displayAllEvents();
        db.closeConnection();
        if(result != null){
            return ResponseEntity.ok(result);
        }else{
            return ResponseEntity.badRequest().body(null);
        }

    }
    @PostMapping("/getAttendanceEvent")
    public ResponseEntity<String> getAttendanceEvent(@RequestBody String eventName,Authentication authentication) {

        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized. Admin privileges required.");
        }
        if (eventName == null ) {
            return ResponseEntity.badRequest().body("Event name is required.");
        }
        ManageDB db = new ManageDB();
        String result = db.obterInfoUtilizadoresPorEvento(eventName);
        db.closeConnection();
        if(result != null){
            return ResponseEntity.ok(result);
        }else{
            return ResponseEntity.badRequest().body("Event not found");
        }

    }
    @PostMapping("/generateCode")
    public ResponseEntity<String> generateCode(@RequestBody CodigoRegisto code,Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized. Admin privileges required.");
        }
        ManageDB db = new ManageDB();
        if (code.getHoraFinal() == null) {
            db.closeConnection();
            return ResponseEntity.badRequest().body("Hora_Final cannot be null.");
        }
            db.insertCodigoRegisto(code);
        db.closeConnection();
        return ResponseEntity.ok("Código gerado com sucesso");

    }
}
