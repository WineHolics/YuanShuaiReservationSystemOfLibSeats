package ys.controller;

// 座位管理
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import ys.entity.Choice;
import ys.entity.ComboValue;
import ys.entity.Room;
import ys.entity.Score;
import ys.entity.Seat;
import ys.entity.User;
import ys.service.ChoiceService;
import ys.service.RoomService;
import ys.service.ScoreService;
import ys.service.SeatService;
import ys.util.WriterUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sun.org.apache.bcel.internal.generic.NEW;
@Controller
@RequestMapping("seat")
public class SeatController {

	private int page;
	private int rows;
	@Autowired
	private SeatService<Seat> seatService;
	private Seat seat;
	@Autowired
	private RoomService<Room> roomService;
	@Autowired
	private ChoiceService<Choice> choiceService;
	private Choice choice;
	@Autowired
	private ScoreService<Score> scoreService;
	private Score score;
	
	@RequestMapping("seatIndex")
	public String index(){
		return "seat/selectSeat";
	}
	
	
	@RequestMapping("combolist")
	public void seatList(HttpServletRequest request,HttpServletResponse response) {
		try {
			seat = new Seat();
			String date = request.getParameter("date");
			if(date==null || date.length()==0){
				seat.setDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
			}else {
				seat.setDate(date);
			}
			if(request.getParameter("roomid")!=null && request.getParameter("roomid").length() > 0){
				seat.setRoomid(Integer.parseInt(request.getParameter("roomid")));
			} else {
				seat.setRoomid(1);
			}
			String time = request.getParameter("time");
			if(time == null || time.length()==0){
				seat.setTime("08:00-12:00");
			}else {
				seat.setTime(time);
			}
			List<Seat> list = seatService.findSeat(seat);
			JSONArray array = new JSONArray();
			array.addAll(list);
			WriterUtil.write(response, array.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//date下拉框：今天和明天
	@RequestMapping("dateCombo") 
	public void dateCombo(HttpServletRequest request,HttpServletResponse response){
		try {
			// 获取今明两天时间的String值。格式是yyyy-MM-dd
			Date todayDate = new Date();
			Date tomorrowDate = getNextDay(todayDate);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String today = sdf.format(todayDate);
			String tomorrow = sdf.format(tomorrowDate);
			
			List<ComboValue> list = new ArrayList<ComboValue>();
			ComboValue cv = new ComboValue(today, "今天  "+today);
			list.add(cv);
			ComboValue cv2 = new ComboValue(tomorrow, "明天  "+tomorrow);
			list.add(cv2);
			
			JSONArray array = new JSONArray();
			array.addAll(list);
			WriterUtil.write(response, array.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// 获取明天日期
	public static Date getNextDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		date = calendar.getTime();
		return date;
	}
	
	
	
	//时间段下拉框：3个时间段
	@RequestMapping("timeCombo")
	public void timeCombo(HttpServletRequest request,HttpServletResponse response) {
		try {
			List<ComboValue> list = new ArrayList<ComboValue>();
			ComboValue cv =  new ComboValue("08:00-12:00","08:00-12:00");
			list.add(cv);
			ComboValue cv2 = new ComboValue("14:00-18:00","14:00-18:00");
			list.add(cv2);
			ComboValue cv3 = new ComboValue("18:00-22:00","18:00-22:00");
			list.add(cv3);
			JSONArray array = new JSONArray();
			array.addAll(list);
			WriterUtil.write(response, array.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	// 阅览室下拉框
	@RequestMapping("roomCombo")
	public void roomCombo(HttpServletRequest request,HttpServletResponse response){
		try {
			List<Room> list = roomService.findRoom(new Room());
			JSONArray array = new JSONArray();
			array.addAll(list);
			WriterUtil.write(response, array.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	//查找自己的座位
	@RequestMapping("myselfSeat")
	public void myselfSeat(HttpServletRequest request,HttpServletResponse response){
		User currentUser = (User)request.getSession().getAttribute("currentUser");
		try {
			String date = request.getParameter("date");
			if(date==null || date.length()==0){
				date = (new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
			}
			String roomid = request.getParameter("roomid");
			if(roomid==null || roomid.length()==0){
				roomid = "1";
			} 
			String time = request.getParameter("time");
			if(time == null || time.length()==0){
				time = "08:00-12:00";
			}
			Choice c = new Choice();
			c.setSeatkeyword(date + "-" +time + "-" +roomid);
			c.setStudentno(currentUser.getUserName());
			choice = choiceService.findOneChoice(c);
			if(choice == null){
				WriterUtil.write(response, "no");
			} else {
				WriterUtil.write(response, choice.getSeatkeyword());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	// 保存选中座位
	@RequestMapping("saveSeat")
	public void saveSeat(HttpServletRequest request,HttpServletResponse response) {
		String keyword = request.getParameter("keyword");
		System.out.println("key---"+keyword);
		System.out.println("key---"+keyword.length());
		User currentUser = (User)request.getSession().getAttribute("currentUser");
		if(currentUser.getRoleId() == 1  || currentUser.getRoleId()==2){  //超管和教师不能选座
			WriterUtil.write(response, "对不起，该阅览室选座只对学生开放");
			return;
		}
		String studentno = currentUser.getUserName();
		String nowDateHour = new SimpleDateFormat("yyyy-MM-dd-HH").format(new Date());  //当前小时数
		//String selectedDate = keyword.substring(0,13);  
		try {
			//判断信用积分
			score = scoreService.findOneScore(studentno);
			int myScore = score.getTotal();     //该学生分数
			
			int roomid = Integer.parseInt(keyword.substring(23,24));
			int needScore = roomService.findScoreByRoomid(roomid);
			if(needScore >= myScore){
				WriterUtil.write(response, "预约失败！您的信用积分不允许在该阅览室选座");
				return;
			}
			String flag = "1";
			Choice c = new Choice();
			c.setStudentno(studentno);
			c.setStatus("0");
			List<Choice> list = choiceService.findChoice(c);
			if(list==null || list.size()==0){
				// 无预约 OK的
			} else if(list.size()>3){
				// 限预约3次
				flag = "3";
			}else {
				for(Choice choice : list){
					String time = choice.getSeatkeyword().substring(0,22);
					if(time.equals(keyword.substring(0,22))){
						//重复了
						flag = "2";
						break;
					}
				}	
			}
			if("3".equals(flag)){
				WriterUtil.write(response, "预约失败！24小时之类已经预约3次了");return;
			} else if ("2".equals(flag)) {
				WriterUtil.write(response, "预约失败！这个时间段已经预约过其他阅览室了");return;
			} else {
				choice = new Choice();
				choice.setStudentno(studentno);
				choice.setSeatkeyword(keyword);
				choice.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				choiceService.addChoice(choice);
				
				seat = new Seat();
				seat.setKeyword(keyword);
				seat.setStudentno(studentno);
				seatService.occupySeat(seat);
				
				WriterUtil.write(response, "ok");
			}
		} catch (Exception e) {
			e.printStackTrace();
			WriterUtil.write(response, "对不起！系统错误，选座失败！");
		}
	}
	
	
	
	
	
	//取消
	@RequestMapping("cancelSeat")
	public void cancelSeat(HttpServletRequest request,HttpServletResponse response){
		User currentUser = (User)request.getSession().getAttribute("currentUser");
		try {
			// 删除choice表中的记录
			String keyword = request.getParameter("seatkeyword");
			System.out.println("keyword========="+keyword);
			System.out.println("keyword length========="+keyword.length());
			Choice choice = new Choice();
			choice.setSeatkeyword(keyword.substring(0, 20));
			choice.setStudentno(currentUser.getUserName());
			choiceService.cancelChoice(choice);
			
			// 将seat表中该条记录学号变成1
			seatService.cancelSeat(keyword); //
			
			WriterUtil.write(response, "ok");
		} catch (Exception e) {
			e.printStackTrace();
			WriterUtil.write(response, "对不起！取消失败");
		}
	}
	
	
}
