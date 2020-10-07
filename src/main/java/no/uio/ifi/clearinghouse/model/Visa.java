package no.uio.ifi.clearinghouse.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * POJO representing Crypt4GH visa.
 */
@Data
@ToString
@NoArgsConstructor
public class Visa {

    private String sub;                         // JWT subject

    @NonNull
    private String type;                        // passport visa type

    @NonNull
    private Long asserted;                      // seconds since epoch

    @NonNull
    private String value;                       // value string

    @NonNull
    private String source;                      // source URL

    private List<List<Map<?, ?>>> conditions;   // conditions

    private String by;                          // by identifier

}
