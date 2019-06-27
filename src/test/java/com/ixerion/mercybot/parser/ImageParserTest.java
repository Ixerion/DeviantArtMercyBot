package com.ixerion.mercybot.parser;

import com.ixerion.mercybot.entity.Image;
import com.ixerion.mercybot.builder.Query;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.ixerion.mercybot.util.PropConstants.*;

public class ImageParserTest {

    private ImageParser parser;
    private Query query;

    @BeforeClass
    public void setUp() {
        parser = new ImageParser();
        query = new Query()
                .setSearchObject(QUERY.getValue())
                .setFilter(THREE_DAYS_FILTER.getValue())
                .setAmount(Integer.valueOf(IMAGE_LIMIT.getValue()));
    }

    @Test
    public void imageParserTest() throws IOException {
        int expectedAmount = 3;
        List<Image> imageElements = parser.findDeviantartImageElements(query);
        int actualAmount = imageElements.size();
        Assert.assertEquals(expectedAmount, actualAmount);
    }
}
