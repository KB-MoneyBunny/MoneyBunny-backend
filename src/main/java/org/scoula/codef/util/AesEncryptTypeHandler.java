package org.scoula.codef.util;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AesEncryptTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        // VO → DB 저장할 때 자동 암호화
        if (parameter == null) {
            ps.setString(i, null);
        } else {
            ps.setString(i, AesUtil.encrypt(parameter));
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // DB → VO 매핑 시 자동 복호화
        String encrypted = rs.getString(columnName);
        return encrypted != null ? AesUtil.decrypt(encrypted) : null;
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String encrypted = rs.getString(columnIndex);
        return encrypted != null ? AesUtil.decrypt(encrypted) : null;
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String encrypted = cs.getString(columnIndex);
        return encrypted != null ? AesUtil.decrypt(encrypted) : null;
    }
}

