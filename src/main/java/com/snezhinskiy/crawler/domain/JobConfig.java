package com.snezhinskiy.crawler.domain;

import com.snezhinskiy.crawler.domain.embedded.ContentType;
import com.snezhinskiy.crawler.domain.embedded.UploadMethod;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.sql.Time;

@Setter
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="job_config")
public class JobConfig {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "job_config_id_seq_gen")
    @GenericGenerator(
        name = "job_config_id_seq_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @org.hibernate.annotations.Parameter(name = "sequence_name", value = "job_config_id_seq"),
            @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
            @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
        }
    )
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parser_map_id")
    protected SourceParserMap parserMap;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_method")
    private UploadMethod uploadMethod;

    private String name;
    private Integer scheduleInterval;
    private Time scheduleTime;
    private String url;
    private String paginationSelector;
    private String itemsSelector;
    private Integer idleTimeout;
    private boolean enabled;
}


