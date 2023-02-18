package it.polimi.tiw.beans;

import java.sql.Date;

public record File(
        int id,
        String name,
        String extension,
        int parent,
        int owner,
        Date creation,
        String summary,
        String type) {

}
