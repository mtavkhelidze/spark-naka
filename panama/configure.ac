AC_INIT([naka], [0.1.0])
AC_CONFIG_SRCDIR([naka/src/epoch.cpp])
AC_CONFIG_MACRO_DIRS([m4])
AC_CONFIG_AUX_DIR([scripts])
AM_INIT_AUTOMAKE([foreign -Wall subdir-objects])
AC_PROG_CXX
AX_CXX_COMPILE_STDCXX([20], [noext], [mandatory])
AM_CXXFLAGS="-march=native -O3 -I\$(top_srcdir)/naka/include  -I\$(top_srcdir)/fast/include"
AC_SUBST([AM_CXXFLAGS])

AM_PROG_AR
LT_INIT([dlopen shared])

AC_CONFIG_FILES([Makefile fast/Makefile naka/Makefile])
AC_OUTPUT
