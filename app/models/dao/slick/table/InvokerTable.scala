package models.dao.slick.table

trait InvokerTable extends CommonTable {

  import dbConfig.profile.api._
  
  class InnerCommonTable(tag: Tag) extends Table[models.Invoker](tag, "invokers")  with DynamicSortBySupport.ColumnSelector {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ownerId = column[Long]("owner_id")
    def address = column[String]("address")
    def privateKey = column[String]("private_key")
    def registered = column[Long]("registered")
    def * = (
      id,
      ownerId,
      address,
      privateKey,
      registered) <> [models.Invoker](t => models.Invoker(
            t._1,
            t._2,
            t._3,
            t._4,
            t._5), t => Some((
      t.id,
      t.ownerId,
      t.address,
      t.privateKey,
      t.registered)))
    override val select = Map(
      "id" -> (this.id),
      "ownerID" -> (this.ownerId),
      "address" -> (this.address),
      "privateKey" -> (this.privateKey),
      "registered" -> (this.registered))
  }

  val table = TableQuery[InnerCommonTable]
  
}
