package com.example.demo.repository;

import com.example.demo.Common.Text;
import com.example.demo.Utils.RSAUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Repository
public class TextRepository {

    private JdbcTemplate jdbcTemplate;

    public TextRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }

    public void insert(Text text) throws Exception{
        try{
            SimpleJdbcCall simpleJdbcCall=new SimpleJdbcCall(this.jdbcTemplate);
                simpleJdbcCall.withProcedureName("TextInsert");
            MapSqlParameterSource params= new MapSqlParameterSource();
            params.addValue("hash_text",text.getHash_text())
                    .addValue("text_hash",text.getText_hash())
                    .addValue("private_key",text.getPrivate_key());

            simpleJdbcCall.execute(params);

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public String getText(String hash_text) throws Exception{

        String decryptText="";
        try{
            SimpleJdbcCall simpleJdbcCall=new SimpleJdbcCall(this.jdbcTemplate);
            simpleJdbcCall.withProcedureName("GetText");
            MapSqlParameterSource params= new MapSqlParameterSource();
            params.addValue("hashed_text",hash_text);

            Map<String, Object> out =simpleJdbcCall.execute(params);
            List<Text> listText= (List<Text>) out.get("#result-set-1");

            Map map=(Map) listText.get(0);
            decryptText=(String) map.get("text_hash");
            //decryptText=RSAUtils.decrypt(hash_text, private_key);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return decryptText;
    }

    public Text getPrivate_Key(String hash_text) throws Exception{

        Text text=new Text();
        String private_key="";
        try{
            SimpleJdbcCall simpleJdbcCall=new SimpleJdbcCall(this.jdbcTemplate);
            simpleJdbcCall.withProcedureName("GetText");
            MapSqlParameterSource params= new MapSqlParameterSource();
            params.addValue("hashed_text",hash_text);

            Map<String, Object> out =simpleJdbcCall.execute(params);
            List<Text> listText= (List<Text>) out.get("#result-set-1");

            Map map=(Map) listText.get(0);
            text.setText_hash((String) map.get("text_hash"));
            text.setHash_text((String) map.get("hash_text"));
            text.setPrivate_key((String) map.get("private_key"));

//            byte[] buff=private_key.getBytes(StandardCharsets.UTF_8);
//
//            decryptText= new String(buff);
            //text=RSAUtils.decrypt(hash_text, private_key);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return text;
    }
}
