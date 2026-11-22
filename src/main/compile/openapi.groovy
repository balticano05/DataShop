import io.javalin.openapi.experimental.ExperimentalCompileOpenApiConfiguration
import io.javalin.openapi.experimental.OpenApiAnnotationProcessorConfiguration
import io.javalin.openapi.experimental.OpenApiAnnotationProcessorConfigurer
import io.javalin.openapi.experimental.SimpleType

@ExperimentalCompileOpenApiConfiguration
class OpenApiConfiguration implements OpenApiAnnotationProcessorConfigurer {

    @Override
    void configure(OpenApiAnnotationProcessorConfiguration configuration) {
        configuration.simpleTypeMappings['java.math.BigDecimal'] = new SimpleType('number', 'double')
    }
}
