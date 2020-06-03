package ys.controller;

// 学生违规控制器
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import ys.entity.Illegal;
import ys.entity.Score;
import ys.entity.Student;
import ys.service.IllegalService;
import ys.service.ScoreService;
import ys.util.WriterUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("illegal")
public class IllegalController {

	private int page;
	private int rows;
	private Illegal illegal;
	@Autowired
	private IllegalService<Illegal> illegalService;
	@Autowired
	private ScoreService<Score> scoreService;
	private Score score;
	
	@RequestMapping("illegalList")
	public void illegalList(HttpServletRequest request,HttpServletResponse response){
		try {
			page = Integer.parseInt(request.getParameter("page"));
			rows = Integer.parseInt(request.getParameter("rows"));
			illegal = new Illegal();
			illegal.setPage((page-1)*rows);
			illegal.setRows(rows);
			illegal.setStudentno(request.getParameter("studentno"));
			List<Illegal> list = illegalService.findIllegal(illegal);
			int total = illegalService.countIllegal(illegal);//根据学生id查找违规记录条数
			JSONObject jsonObj = new JSONObject();//new一个JSON
			jsonObj.put("total",total );//total代表一共有多少数据
			jsonObj.put("rows", list);//row是代表显示的页的数据
	        WriterUtil.write(response,jsonObj.toString()); //将上述Json输出，前台ajax接收
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@RequestMapping("reserveIllegal")
	public void reserveIllegal(HttpServletRequest request,HttpServletResponse response,Illegal illegal) {
		JSONObject result = new JSONObject();
		result.put("success", true);
		try {
			String studentno = illegal.getStudentno();     //获得学号
			score = scoreService.findOneScore(studentno);  //根据学号查询积分信息
			int total = score.getTotal();                  //获得原始积分
			int thisScore = illegal.getScore();             //本次扣除积分
			if(thisScore > total){
				result.put("errorMsg", "对不起！扣除失败，要扣除的分数大于剩余积分！");
			} else {
				illegalService.addIllegal(illegal);  //添加违规记录
				// 更新score表中的总积分
				score.setTotal(total - thisScore);
				scoreService.updateScore(score);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("errorMsg", "对不起，操作失败");
		}
		WriterUtil.write(response, result.toString());
	}
	
	@RequestMapping("deleteIllegal")
	public void delete(HttpServletRequest request,HttpServletResponse response){
		JSONObject result=new JSONObject();
		try {
			String[] ids=request.getParameter("ids").split(",");//获取选中的违规记录ID
			for (String id : ids) {
				illegalService.deleteIllegal(Integer.parseInt(id));//数据库删除选中的违规记录
			}
			result.put("success", true);
			result.put("delNums", ids.length);//删除的记录条数
		} catch (Exception e) {
			e.printStackTrace();
			result.put("errorMsg", "对不起，删除失败");
		}
		WriterUtil.write(response, result.toString());
	}
		
}
