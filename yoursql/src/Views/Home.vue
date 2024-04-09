// src/views/Home.vue
<script>
import axios from "axios";
import {ref} from "vue";
import Table from "@/Components/Table.vue";
import History from "@/Components/History.vue";
import SqlHistory from "@/Components/SqlHistory.vue";
import SignInSignUp from "@/Views/SignInSignUp.vue";

export default {
  name: "Home",
  components: {SignInSignUp, SqlHistory, History, Table},
  data(){
    return{
      sql:"",
      messages:ref([]),
      responsemessage:ref(""),
      tables:ref([]),
      count:1,
      colors: ['#ff9999', '#99ff99', '#9999ff', '#ffff99', '#ff99ff', '#99ffff', '#ffa07a', '#7fffd4', '#ffd700', '#20b2aa'], // 颜色数组
    }
  },
  computed: {
    // 计算属性，根据索引返回对应的背景颜色
    getBackgroundColor() {
      return (index) => {
        // 使用索引对颜色数组取模，以确保索引超出颜色数组长度时仍然能够获取到颜色
        return this.colors[index % this.colors.length];
      };
    },
  },
  methods:{
    async executesql(index,sqlmessage) {
      try {
        console.log(sqlmessage);
        this.sql=sqlmessage;
        const response = await axios.post("http://localhost:8080/processsql", { sql: this.sql });// 发送SQL语句作为请求主体
        if(response.data.code===0) {
          console.log(response.data);
          this.messages = response.data.data;
          console.log(this.messages);
          this.tables[index] = this.messages;
          this.responsemessage = response.data.message;
          this.count++;
          console.log(this.tables);
          console.log(this.responsemessage);
          this.$refs.sqlHistoryRef.appendMessage(this.responsemessage + ': ' + this.sql);
          this.sql="";
        }else if(response.data.code===1){
          this.responsemessage=response.data.message;
          this.count++;
          this.$refs.sqlHistoryRef.appendMessage(this.responsemessage + ': ' + this.sql);

        }
      } catch (e) {
        console.error(e);
      }
    },
    async executeSqlFile(file) {
      try {
        // 读取文件内容
        const reader = new FileReader();
        reader.onload = async (event) => {
          const fileContent = event.target.result;
          const sanitizedContent = fileContent.replace(/[\n\r\t]/g, '');

// 按照分号分割文件内容，得到每一行 SQL 语句
          const sqlStatements = sanitizedContent.split(';');

// 遍历每一条 SQL 语句，并执行
          for (let i = 0; i < sqlStatements.length; i++) {
            let sql = sqlStatements[i].trim();
            // 检查是否为最后一条非空 SQL 语句
            if (sql !== '' && i !== sqlStatements.length - 1) {
              sql += ';'; // 在非最后一条语句后添加分号
            }
            if (sql !== '') {
              await this.executesql(this.count, sql);
            }
          }

        }

        // 以文本格式读取文件
        reader.readAsText(file);
      } catch (error) {
        console.error('Error reading SQL file:', error);
      }
    },




    async execute(index) {
      try {
        console.log(this.sql);
        const response = await axios.post("http://localhost:8080/processsql", { sql: this.sql });// 发送SQL语句作为请求主体
        if(response.data.code===0) {
          console.log(response.data);
          this.messages = response.data.data;
          console.log(this.messages);
          this.tables[index] = this.messages;
          this.responsemessage = response.data.message;
          this.count++;
          console.log(this.tables);
          console.log(this.responsemessage);
          this.$refs.sqlHistoryRef.appendMessage(this.responsemessage + ': ' + this.sql);
          this.sql="";
        }else if(response.data.code===1){
          this.responsemessage=response.data.message;
          this.count++;
          this.$refs.sqlHistoryRef.appendMessage(this.responsemessage + ': ' + this.sql);

        }
      } catch (e) {
        console.error(e);
      }
    },
    closetable(index) {
      console.log(this.tables);
      this.tables[index]=Array().fill().map(()=>[]); // 使用 $set 方法确保 Vue 能够正确地响应数据变化
      console.log(this.tables);
    },

  },
  mounted() {
    this.tables = Array(10).fill().map(() => []);
    this.responsemessage = [];
  }

}
</script>
<!--['#ff9999', '#99ff99', '#9999ff', '#ffff99', '#ff99ff', '#99ffff', '#ffa07a', '#7fffd4', '#ffd700', '#20b2aa']-->
<template>
  <div id="home" >
    <div id=container>
    <div id="title">
      <div id="inside">
        <div style="color:#E16363">Y</div>
        <div style="color:#FFB74D">o</div>
        <div style="color:#9FA8DA">u</div>
        <div style="color:#E16363">r</div>
        <div style="color:#EC407A">S</div>
        <div style="color:#FFCC80">q</div>
        <div style="background-color:#4A90E2; color:#FFFFFF;">l</div>

      </div>
    </div>
      <div id="pagebody">
    <input v-model="this.sql" @keyup.enter="execute(count%10)" placeholder="Press Enter To Execute  SQL"/>

    <div v-for="(table, index) in tables" :key="index" id="tables">
      <Table  v-if="table.length>0" :messages="table" :index="index" @close="closetable(index)" :style="{ backgroundColor: getBackgroundColor(index) }"/>
    </div>

        </div>

      </div>
    <div id="history">
      <SqlHistory ref="sqlHistoryRef"/>
    </div>



  </div>

</template>

<style scoped>
#home{
  width:1016px;
  height:100%;
  background-color:transparent;
  transform:translate(500px,200px);
  display:flex;
  flex-direction:row;
}
#history{
  transform:translate(165px,-200px);
  z-index:1000;
  overflow-x:hidden;
}
#history::-webkit-scrollbar{
  width:0;
  height:0;
}

input{
  background-color:transparent;
  width:500px;
  height:50px;
  margin-top:100px;
  border:none;
  border-bottom:1px solid white;
  color:white;
  font-size:25px;
  padding-left:20px;
  z-index:1000;
  font-weight:700;


}
#title{
  color:white;
  font-size:100px;
  font-weight:600;
  transform:translate(70px,50px);
  z-index:1000;
  user-select: none;
}
#pagebody{
  display:flex;
  flex-direction:row;
}
#tables{
  transform:translate(-500px,155px);
}
input:focus{
  outline:none;
}
#container{
  display:flex;
  flex-direction:column;
}
#submit{
  width:100px;
  height:30px;
  background-color:transparent;
  color:white;
  font-size:15px;
  font-weight:600;
  transform:translate(-200px,121px);
  border:none;
  z-index:1000;
}
::placeholder{
  text-indent:120px;
  color: lightgray; /* 设置颜色 */
  font-size: 18px; /* 设置字体大小 */
  font-weight:400;
  user-select: none;
}
#inside{
  display:flex;
  flex-direction:row;
}
</style>