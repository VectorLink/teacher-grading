<template>
  <div class="app-container">
    <h2 class="title">{{templateName}}</h2>
    <p class="method">方式：涂色评分、简单选择</p>
    <div class="teacher-info">
      <p>评价教师：{{teacherName}}</p>
      <el-button type="primary" round @click="submitFeedback">提交问卷</el-button>
    </div>
    <!-- 表格样式表单 -->
    <el-form :model="form" class="form-table">
      <div class="form-header">
        <div class="form-cell cell-content">评价内容</div>
        <div class="form-cell cell-method">评价方式</div>
        <div class="form-cell cell-input">记录栏</div>
      </div>

      <div
        class="form-row"
        v-for="(item, index) in tableData"
        :key="index"
      >
        <div class="form-cell cell-content">{{ item.content }}</div>
        <div class="form-cell cell-method">{{ item.method }}</div>
        <div class="form-cell cell-input">
          <el-input
            v-model="form[item.field]"
            size="small"
            placeholder="请填写"
            clearable
          />
        </div>
      </div>
    </el-form>

    <!-- 评分标准 -->
    <div class="scoring-guidelines">
      <h3>评分标准：</h3>
      <ol>
        <li>1.我喜欢的老师（一颗星星0分，两颗星星1分，三颗星星2分）</li>
        <li>2.老师最有趣的时候（无趣1分，偶尔有趣2分，什么时候都有趣3分）</li>
        <li>3.老师的帮助：我伤心的时候，老师会...（没有反应1分，短暂安慰2分，主动安慰3分）</li>
        <li>4.我想对老师说（无话可说1分，其他2分）</li>
      </ol>
    </div>
  </div>
</template>

<script>
import {getQuestionnaire, submitQuestionnaire} from '@/api/teacher/grading'

export default {
  name: "biggerGarden",
  data() {
    return {
      form: {
        teacher: '',
        funnyTime: '',
        teacherHelp: '',
        speakToTeacher: ''
      },
      tableData: [
        {
          field: 'teacher',
          content: '我喜欢的老师',
          method: '涂星星评价（1-3颗星星）'
        },
        {
          field: 'funnyTime',
          content: '老师最有趣的时候',
          method: '老师带我们玩____'
        },
        {
          field: 'teacherHelp',
          content: '老师的帮助：我伤心的时候，老师会...',
          method: '选择：抱抱我/擦眼泪/____'
        },
        {
          field: 'speakToTeacher',
          content: '我想对老师说',
          method: '幼儿口述，教师录音+记录'
        }
      ],
      templateName: "",
      teacherName: '',
      questionnaireId: '',
      questionnaireQueryParam: {
        templateType: 7,
        userId: ''
      },
    }
  },
  computed: {
    hasEmptyRecords() {
      // 检查表单中是否有空值
      return Object.values(this.form).some(value => !value || !value.trim());
    }
  },
  methods: {
    async fetchFeedbackData() {
      try {
        const resp = await getQuestionnaire(this.questionnaireQueryParam);
        if (!resp || !resp.data) {
          this.$message.error('获取数据失败：返回数据格式错误');
          return;
        }

        this.questionnaireId = resp.data.questionnaireId;
        this.templateName = resp.data.templateName;

        // 解析并填充表单数据
        if (resp.data.value) {
          try {
            const valueObj = JSON.parse(resp.data.value);
            this.form = {...this.form, ...valueObj};
          } catch (e) {
            console.error('解析数据失败:', e);
            this.$message.error('解析问卷数据失败，请联系管理员');
          }
        }
      } catch (error) {
        console.error('获取问卷数据失败:', error);
        this.$message.error('获取问卷数据失败，请稍后重试');
      }
    },
    async submitFeedback() {
      // 验证是否有空的记录
      if (this.hasEmptyRecords) {
        this.$message.warning('请完成所有评价项的记录');
        return;
      }

      try {
        // 构建请求载荷
        const payload = {
          questionnaireId: this.questionnaireId,
          value: JSON.stringify(this.form)
        };

        // 调用API提交数据
        const response = await submitQuestionnaire(payload);

        if (response && response.code === 200) {
          this.$message.success('评价提交成功');
        } else {
          this.$message.error((response && response.msg) || '提交失败，请稍后重试');
        }
      } catch (error) {
        console.error('提交评价数据失败:', error);
        this.$message.error('提交失败，请稍后重试');
      }
    }
  },
  created() {
    this.teacherName = this.$route.query.nickName || '未指定';
    this.questionnaireQueryParam.userId = this.$route.query.userId || '';
    this.fetchFeedbackData();
  }
}
</script>

<style scoped>
.app-container {
  width: 100%;
  padding: 20px;
  font-family: "Microsoft YaHei", sans-serif;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  box-sizing: border-box;
}

.title {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 12px;
  color: #303133;
  padding-top: 10px;
}

.method {
  text-align: center;
  font-size: 14px;
  margin-bottom: 20px;
  color: #606266;
}

.teacher-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 5px;
}

.form-table {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 30px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
}

.form-header,
.form-row {
  display: flex;
  width: 100%;
  border-bottom: 1px solid #ebeef5;
}

.form-row:last-child {
  border-bottom: none;
}

.form-header {
  background-color: #f2f6fc;
  font-weight: bold;
  color: #303133;
}

.form-cell {
  padding: 12px 15px;
  border-right: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  box-sizing: border-box;
}

.form-cell:last-child {
  border-right: none;
}

.cell-content {
  width: 20%;
  min-width: 150px;
  font-weight: 500;
}

.cell-method {
  width: 40%;
  color: #606266;
}

.cell-input {
  width: 40%;
  padding: 8px 15px;
}

h3 {
  font-size: 18px;
  margin-top: 25px;
  margin-bottom: 15px;
  position: relative;
  padding-left: 12px;
  color: #303133;
}

h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background-color: #409eff;
  border-radius: 2px;
}

ol {
  padding-left: 20px;
  margin: 0;
}

ol li {
  line-height: 2;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

/* 响应式设计优化 */
@media screen and (max-width: 768px) {
  .app-container {
    padding: 15px 10px;
    margin: 0;
    border-radius: 0;
    box-shadow: none;
  }

  .teacher-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .form-header,
  .form-row {
    flex-direction: column;
  }

  .form-cell {
    width: 100% !important;
    border-right: none;
    border-bottom: 1px solid #ebeef5;
  }

  .form-row .form-cell:last-child {
    border-bottom: none;
  }

  .cell-content,
  .cell-method,
  .cell-input {
    padding: 10px;
  }

  .cell-input {
    padding: 5px 10px 10px;
  }

}
</style>
