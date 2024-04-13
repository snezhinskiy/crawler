package com.snezhinskiy.crawler.processing.model.map;

import com.snezhinskiy.crawler.processing.model.ParseRule;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductParserMapDTO extends BaseContentParserMapDTO {
    private List<ParseRule> nameRules;
    private List<ParseRule> skuRules;
    private List<ParseRule> descriptionRules;
    private List<ParseRule> previewRules;
    private List<ParseRule> priceRules;
    private List<ParseRule> stockRules;
}
