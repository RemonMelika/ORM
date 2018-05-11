/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orm;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ORM {
     public static void main(String[] args) {
        
        // TODO code application logic here
        //dbAdapter dbAdapter1 = dbAdapter.getInstance();
        //System.out.println(dbAdapter1.toString());
//         try {
//             System.out.println(dbAdapter1.getConnection().getCatalog());
//         } catch (SQLException ex) {
//             Logger.getLogger(ORM.class.getName()).log(Level.SEVERE, null, ex);
//         }
         //Entity entity = new Entity();
         User user =new User("peter", "23" );
         user.Insert();
         User user1 =new User("Sam", "24");
         user1.Insert();
         user.setName("Marc");
         user.Insert();
    }
}
