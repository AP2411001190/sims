package com.sims;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** All JDBC operations: Create, Retrieve, Update, Delete, Search. */
public class StudentDAO {

    public void add(Student s) throws SQLException {
        String sql = "INSERT INTO students (reg_no,name,email,phone,department,year,cgpa) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, s);
            ps.executeUpdate();
        }
    }

    public void update(Student s) throws SQLException {
        String sql = "UPDATE students SET reg_no=?,name=?,email=?,phone=?,department=?,year=?,cgpa=? WHERE id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, s);
            ps.setInt(8, s.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM students WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Student> getAll() throws SQLException {
        return query("SELECT * FROM students ORDER BY id", null);
    }

    public List<Student> search(String keyword) throws SQLException {
        return query("SELECT * FROM students WHERE name LIKE ? OR reg_no LIKE ? OR department LIKE ? ORDER BY id",
                "%" + keyword + "%");
    }

    private List<Student> query(String sql, String like) throws SQLException {
        List<Student> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (like != null) { ps.setString(1, like); ps.setString(2, like); ps.setString(3, like); }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(rs.getInt("id"), rs.getString("reg_no"), rs.getString("name"),
                            rs.getString("email"), rs.getString("phone"), rs.getString("department"),
                            rs.getInt("year"), rs.getDouble("cgpa")));
                }
            }
        }
        return list;
    }

    private void bind(PreparedStatement ps, Student s) throws SQLException {
        ps.setString(1, s.getRegNo());
        ps.setString(2, s.getName());
        ps.setString(3, s.getEmail());
        ps.setString(4, s.getPhone());
        ps.setString(5, s.getDepartment());
        ps.setInt(6, s.getYear());
        ps.setDouble(7, s.getCgpa());
    }
}
