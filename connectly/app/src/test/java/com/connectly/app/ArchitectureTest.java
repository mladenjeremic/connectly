package com.connectly.app;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.connectly", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainIsPure = noClasses().that().resideInAPackage("com.connectly.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..",
                    "com.connectly.application..", "com.connectly.infrastructure..", "com.connectly.presentation..");

    @ArchTest
    static final ArchRule applicationIsIsolated = noClasses().that().resideInAPackage("com.connectly.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "com.connectly.infrastructure..", "com.connectly.presentation..", "jakarta.persistence..");

    @ArchTest
    static final ArchRule presentationDoesNotKnowInfra = noClasses().that().resideInAPackage("com.connectly.presentation..")
            .should().dependOnClassesThat().resideInAPackage("com.connectly.infrastructure..");
}