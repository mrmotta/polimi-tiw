package it.polimi.tiw.beans;

import java.util.ArrayList;
import java.util.List;

public record Folder(
        int id,
        String name,
        int owner,
        int parent,
        List<Folder> subfolders) {

    public Folder(int id, String name, int owner, int parent, List<Folder> subfolders) {
        this.id = id;
        this.name = name;
        this.subfolders = subfolders != null ? new ArrayList<>(subfolders) : new ArrayList<>();
        this.owner = owner;
        this.parent = parent;
    }

    public boolean isSubfolder() {
        return parent != -1;
    }
}
