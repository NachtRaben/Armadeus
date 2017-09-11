package com.nachtraben.core.util.images;

import net.kodehawa.lib.imageboards.ImageboardAPI;
import net.kodehawa.lib.imageboards.entities.BoardImage;
import net.kodehawa.lib.imageboards.util.Imageboards;

import java.util.ArrayList;
import java.util.List;

public class NMappedBoards {

    public static final List<ImageboardAPI<? extends BoardImage>> nsfwBoards = new ArrayList<>();
    public static final List<ImageboardAPI<? extends BoardImage>> cleanBoards = new ArrayList<>();
    public static final List<ImageboardAPI<? extends BoardImage>> allBoards = new ArrayList<>();

    static {
        nsfwBoards.add(Imageboards.E621);
        nsfwBoards.add(Imageboards.KONACHAN);
        nsfwBoards.add(Imageboards.RULE34);
        nsfwBoards.add(Imageboards.YANDERE);
        nsfwBoards.add(Imageboards.DANBOORU);

        cleanBoards.add(Imageboards.KONACHAN);
        cleanBoards.add(Imageboards.YANDERE);
        cleanBoards.add(Imageboards.DANBOORU);
        cleanBoards.add(Imageboards.SAFEBOORU);

        allBoards.add(Imageboards.E621);
        allBoards.add(Imageboards.KONACHAN);
        allBoards.add(Imageboards.RULE34);
        allBoards.add(Imageboards.YANDERE);
        allBoards.add(Imageboards.DANBOORU);
        allBoards.add(Imageboards.SAFEBOORU);
    }

}
