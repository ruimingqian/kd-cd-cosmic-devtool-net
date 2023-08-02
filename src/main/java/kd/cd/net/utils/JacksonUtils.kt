package kd.cd.net.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule

/**
 * @Description: Desc
 * @Author: ZeXian Chen
 * @Date: 2023/8/1
 */
class JacksonUtils private constructor() {
    companion object {
        @JvmStatic
        val objectMapper: ObjectMapper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            val instance = ObjectMapper()
            instance.registerModule(JavaTimeModule()).registerModule(ParameterNamesModule())
                .registerModules(ObjectMapper.findModules())
            //是否允许使用注释
            instance.configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            //允许单引号
            instance.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            //允许转义字符
            instance.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true)
            //不检测失败字段映射
            instance.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            //时间字段输出时间戳
            instance.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
            //时间输出为毫秒而非纳秒
            instance.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            //空对象不出错
            instance.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            //时间读取为毫秒而非纳秒
            instance.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            //不输出空值字段
            instance.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
            instance
        }

        @JvmStatic
        fun readJsonNode(jsonStr: String): JsonNode {
            return objectMapper.readTree(jsonStr)
        }

        @JvmStatic
        fun readObjectNode(jsonStr: String): ObjectNode {
            return readJsonNode(jsonStr) as ObjectNode
        }

        @JvmStatic
        fun getKeySet(node: JsonNode): Set<String> {
            val result = HashSet<String>()
            while (node.fields().hasNext()) {
                val key = node.fields().next().key
                result.add(key)
            }
            return result
        }
    }
}