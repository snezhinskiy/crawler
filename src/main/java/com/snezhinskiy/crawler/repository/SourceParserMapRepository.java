package com.snezhinskiy.crawler.repository;

import com.snezhinskiy.crawler.domain.SourceParserMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceParserMapRepository extends JpaRepository<SourceParserMap, Long> {

}
