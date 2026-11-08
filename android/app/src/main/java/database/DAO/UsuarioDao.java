package database.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import database.MODEL.Usuario;

@Dao
public interface UsuarioDao {

    // Método para cadastrar novo usuário
    @Insert
    void insert(Usuario usuario);

    // Método para validar o login (retorna o usuário se email e senha baterem)
    @Query("SELECT * FROM usuario WHERE email = :email AND senha = :senha LIMIT 1")
    Usuario fazerLogin(String email, String senha);

    // Método para verificar se o e-mail já existe no cadastro
    @Query("SELECT * FROM usuario WHERE email = :email LIMIT 1")
    Usuario verificarEmailExistente(String email);
}