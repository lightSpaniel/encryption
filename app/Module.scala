import play.api.Configuration
import play.api.Environment
import play.api.inject.Binding
import play.api.inject.Module
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.mvc.Controller
import services.{Encrypt, Encryption}
import actors._

class MyModule extends Module {
  def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {

    Seq(bind[Encryption].to[Encrypt])

  }
}

class ActorModule extends AbstractModule with AkkaGuiceSupport {
  def configure = {
    bindActor[ConfiguredActor]("configured-actor")
    bindActor[ParentActor]("parent-actor")
    bindActor[ConfiguredChildActor]("configured-child-actor")
    bindActorFactory[ConfiguredChildActor, ConfiguredChildActor.Factory]
  }
}
