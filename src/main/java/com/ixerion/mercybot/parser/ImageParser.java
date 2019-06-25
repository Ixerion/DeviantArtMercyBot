package com.ixerion.mercybot.parser;

import com.ixerion.mercybot.builder.Query;
import com.ixerion.mercybot.entity.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

public class ImageParser {

    public List<Image> findDeviantartImageElements(Query query) throws IOException {
        Document doc = Jsoup.connect(query.toString()).get();
        List<Element> links = doc.select("[data-super-alt*=Mercy]")
                .stream()
                .limit(query.getAmount())
                .collect(Collectors.toList());
        List<Image> images = new ArrayList<>();
        for (Element el : links) {
            String link = el.attr("data-super-full-img");
            String name = el.attr("data-super-alt") + "-" + randomAlphabetic(3);
            images.add(new Image(link, name));
        }
        return images;
    }

    public List<String> getImages(Query query) {
        /*String query = new Query()
                .setSearchObject(QUERY.getValue())
                .setFilter(filterValue)
                .toString();*/
        List<String> urls = null;
        try {
            urls = findDeviantartImageElements(query).stream().map(Image::getHref).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return urls;
    }
}
