package todo

import cats.implicits.*
import scala.collection.mutable
import todo.data.*

/**
 * The InMemoryModel is a Model that stores all the tasks in RAM, and hence they
 * are lost when the server restarts.
 *
 * You should modify this file.
 */
object InMemoryModel extends Model:
  /* These are the tasks the application starts with. You can change these if you want. */
  val defaultTasks = List(
    Id(0) -> Task(State.completedNow, "Complete Effective Scala Week 2", None, List(Tag("programming"), Tag("scala"))),
    Id(1) -> Task(State.Active, "Complete Effective Scala Week 3", Some("Finish the todo list exercise"), List(Tag("programming"), Tag("scala"), Tag("encapsulation"), Tag("sbt"))),
    Id(2) -> Task(State.Active, "Make a sandwich", Some("Cheese and salad or ham and tomato?"), List(Tag("food"), Tag("lunch")))
  )

  /* Every Task is associated with an Id. Ids must be unique. */
  private val idGenerator = IdGenerator(Id(3))

  /* The idStore stores the associated between Ids and Tasks. We use a
   * LinkedHashMap so we can access elements in insertion order. We need to keep
   * a stable order so the UI doesn't jump around, which would be confusing to
   * the user.
   *
   * Note that this data structure is not safe to use with concurrent access.
   * This doesn't matter in this case study, but in a real situation it would be
   * a problem. In a future week we'll learn the techniques to address this. */
  private val idStore: mutable.LinkedHashMap[Id, Task] =
    mutable.LinkedHashMap.from(defaultTasks)

  def create(task: Task): Id =
    val id = idGenerator.nextId()
    idStore.addOne(id -> task)
    id

  def read(id: Id): Option[Task] =
    idStore.get(id)

  def complete(id: Id): Option[Task] =
    update(id)(task => task.copy(state = State.completedNow))

  def update(id: Id)(f: Task => Task): Option[Task] =
    idStore.updateWith(id)(opt => opt.map(f))

  def delete(id: Id): Boolean =
    val res = idStore.remove(id)
    res match
      case None => false
      case _ => true

  def tasks: Tasks =
    Tasks(idStore)

  def tags: Tags =
    // Option 1
    //Tags(idStore.flatMap((id, task) => task.tags).toList.distinct)
    // Option 2
    val tmp = idStore.map((id, task) => task.tags).toList
    Tags(tmp.flatten.distinct)

  def tasks(tag: Tag): Tasks =
    Tasks(idStore.filter((k,v) => v.tags.contains(tag)))

  def clear(): Unit =
    idStore.clear()
