# camunda-bpm-jgiven
Camunda specific stages and scenarios for the BDD testing tool jgiven


hm. I started this because I had the need to link jgiven Scenario/Stage with camunda engine rule.
But it turns out we can just use


```java

@Deployment(resources = BPMN_RESOURCE)
public class ProcessTest extends ScenarioTest<Given, When, Then> {

    @Rule
    @ProvidedScenarioState
    public final ProcessEngineRule camunda = new ProcessEngineRule();

    @Test
    @As("the '" + PROCESS_KEY + "' process can be deployed")
    public void deploys() {
        // empty
    }
}
```

and

```java
public static class Then extends Stage<Then> {
    @ProvidedScenaroState
    private final ProcessEngineRule camunda;
    
    public Then the_process_$_can_be_found_in_the_repository(@Quoted String processDefinitionKey) {
    assertThat(camunda.getRepositoryService().createProcessDefinitionQuery()
           .processDefinitionKey(processDefinitionKey)
           .latestVersion()
           .singleResult()).isNotNull();

            return self();
        }
    }

```

So I guess, we won't even need an additional extension ... only for some common process specific Given/When/Then stuff but where to start and where to end this ...
