package pt.isec.pd.spring_boot.exemplo3.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import pt.isec.pd.spring_boot.exemplo3.server.MSG;
import pt.isec.pd.spring_boot.exemplo3.server.ManageDB;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserAuthenticationProvider implements AuthenticationProvider
{
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        ManageDB db = new ManageDB();
        if(db.checkPass(username,password) == MSG.NO_REGISTERED_USER || db.checkPass(username,password) ==  MSG.WRONG_PASSWORD) {
            db.closeConnection();
            return null;
        }
        if (username.equals("admin@isec.pt")) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ADMIN"));
            db.closeConnection();
            return new UsernamePasswordAuthenticationToken(username, password, authorities);
        }else {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("NORMAL"));
            db.closeConnection();
            return new UsernamePasswordAuthenticationToken(username, password, authorities);
        }
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
