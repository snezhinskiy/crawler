package com.snezhinskiy.crawler.processing.model.map;

import com.snezhinskiy.crawler.processing.model.ParseRule;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class BaseContentParserMapDTO {
    private List<ParseRule> pageTestRule;
}
