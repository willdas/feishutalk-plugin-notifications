package io.jenkins.plugins.feishu.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

/**
 * @className: FeiShuCardMessageDTO
 * @package: com.dt.message.dto
 * @describe: 卡片消息
 * @author:  willdas
 * @date: 2021/11/02 2:27 下午
 **/
@Builder
@Data
public class FeiShuCardMessageModel {

    private static final String MSG_TYPE = "interactive";
    /**
     * 消息的类型，此处固定填 “interactive”
     */
    private String msg_type;

    /**
     * 消息的内容
     */
    private Card card;

    @Builder
    @Data
    public static class Card {
        /**
         * 卡片配置
         */
        private Config config;

        /**
         * 配置卡片标题部分
         */
        private Header header;

        /**
         * 配置卡片主体内容
         */
        private List<Elements> elements;
    }

    @Builder
    @Data
    public static class Config {
        /**
         * 是否允许卡片被转发。
         * 默认 true
         */
        private boolean wide_screen_mode;
    }

    @Builder
    @Data
    public static class Header {
        /**
         * 配置卡片标题内容
         */
        private Title title;
        /**
         * 卡片标题的主题色
         */
        private String template;
    }

    @Builder
    @Data
    public static class Title {
        /**
         * 消息标识
         */
        private String tag;

        /**
         * 消息文本
         */
        private String content;
    }

    @Builder
    @Data
    public static class Elements {
        /**
         * 文本标识
         */
        private String tag;
        /**
         * 文本
         */
        private Text text;
        /**
         * 扩展属性
         */
        private Extra extra;
    }

    @Builder
    @Data
    public static class Text {
        /**
         * 文本标识
         */
        private String tag;
        /**
         * 消息文本
         */
        private String content;
    }

    @Builder
    @Data
    public static class Extra {
        /**
         * 文本标识
         */
        private String tag;
        /**
         * 文本
         */
        private Text text;
        /**
         * 类型
         */
        private String type;
        /**
         * 网址
         */
        private String url;
    }

    public static FeiShuCardMessageModel buildDTO(String title, String titleColor, List<Elements> elements) {
        final Header header = Header.builder()
                .title(Title.builder().tag(TagEnum.PLAIN_TEXT.getName()).content(title).build())
                .template(titleColor).build();
        final Card card = Card.builder()
                .config(Config.builder().wide_screen_mode(true).build())
                .header(header)
                .elements(elements)
                .build();
        return FeiShuCardMessageModel.builder()
                .msg_type(MSG_TYPE)
                .card(card)
                .build();
    }

    @AllArgsConstructor
    @Getter
    public enum TagEnum {
        TEXT("text"),
        AT("at"),
        PLAIN_TEXT("plain_text"),
        LARK_MD("lark_md"),
        DIV("div"),
        HR("hr"),
        BUTTON("button");

        private String name;
    }

    @AllArgsConstructor
    @Getter
    public enum ExtraTypeEnum {
        PRIMARY("primary");

        private String name;
    }

}
